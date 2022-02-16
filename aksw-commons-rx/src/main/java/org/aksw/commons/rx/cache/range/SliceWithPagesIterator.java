package org.aksw.commons.rx.cache.range;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.aksw.commons.util.ref.RefFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.primitives.Ints;


/**
 * An iterator over a range buffer that blocks if items are not loaded.
 *
 * It is used internally by RangeRequestIterator. RangeRequestIterator is the wrapper that takes care that data is being loaded
 * before this iterator waits for any.
 *
 * The iterator MUST BE CLOSED in order for it to release claimed pages!
 * It auto-closes upon consumption.
 *
 *
 * @author raven
 *
 * @param <T>
 */
public class SliceWithPagesIterator<T>
    extends AbstractIterator<T>
    implements AutoCloseable
{
    private static final Logger logger = LoggerFactory.getLogger(SliceWithPagesIterator.class);

    protected SliceWithPages<T> rangeBuffer;
    protected long currentIndex;

    /** Iterator over a range in the page starting at currentOffset */
    protected Iterator<T> rangeIterator = null;

    /** Number of items read from rangeIterator */
    protected int readsFromCurrentRange = 0;

    protected SliceAccessor<T> pageRange;


    public SliceWithPagesIterator(SliceWithPages<T> page, long currentIndex) {
        super();
        this.rangeBuffer = page;
        this.currentIndex = currentIndex;
        this.pageRange = page.newSliceAccessor();
    }


    @Override
    protected T computeNext() {
        T result;

        // For the current index check the metadata for whether it is covered by a range.
        // If so then we know we can consume all items within that range but once we reach the range's end
        // we need to check (and possibly wait) for more data to become available.
        while (rangeIterator == null || !rangeIterator.hasNext()) {
            currentIndex += readsFromCurrentRange;
            readsFromCurrentRange = 0;

            int indexInPage = Ints.checkedCast(rangeBuffer.getIndexInPageForOffset(currentIndex));

            try (RefFuture<SliceMetaData> ref = rangeBuffer.getMetaData()) {
                SliceMetaData metaData = ref.await();

                ReadWriteLock rwl = metaData.getReadWriteLock();
                Lock readLock = rwl.readLock();
                readLock.lock();

                RangeSet<Long> loadedRanges = metaData.getLoadedRanges();
                RangeMap<Long, List<Throwable>> failedRanges = metaData.getFailedRanges();

                Range<Long> entry = null;
                List<Throwable> failures = null;

                try {
                    // If the index is outside of the known size then abort
                    // long knownSize = metaData.getSize();
                    long maximumSize = metaData.getMaximumKnownSize();
                    if (currentIndex >= maximumSize) {
                        close();
                        return endOfData();
                    } else {

                        // rangeBuffer.getFailedRanges().getEntry(currentIndex);

                        failures = failedRanges.get(currentIndex); // .getEntry(currentIndex);
                        entry = loadedRanges.rangeContaining(currentIndex);

                        if (entry == null && failures == null) {
                            // Wait for data to become available
                            // Solution based on https://stackoverflow.com/questions/13088363/how-to-wait-for-data-with-reentrantreadwritelock

                            Lock writeLock = rwl.writeLock();
                            readLock.unlock();
                            writeLock.lock();

                            try {
                                long knownSize;
                                while ((entry = loadedRanges.rangeContaining(currentIndex)) == null &&
                                        ((knownSize = metaData.getMaximumKnownSize()) < 0 || currentIndex < knownSize)) {
                                    try {
                                        logger.info("Awaiting more data: " + entry + " " + currentIndex + " " + knownSize);
                                        metaData.getHasDataCondition().await();
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            } finally {
                                writeLock.unlock();
                                readLock.lock();
                            }
                        }
                    }
                } finally {
                    readLock.unlock();
                }

                if (failures != null && !failures.isEmpty()) {
                    throw new RuntimeException("Attempt to read a range of data marked with an error",
                            failures.get(0));
                }

                if (entry == null) {
                    close();
                    return endOfData();
                } else {
                    Range<Long> range = Range.atLeast(currentIndex).intersection(entry); //  entry; //.getKey();

                    long startAbs = range.lowerEndpoint();
                    long endAbs = range.upperEndpoint();

                    long rangeLength = endAbs - startAbs;

                    pageRange.claimByOffsetRange(startAbs, endAbs);

                    BufferView<T> buffer = pageRange.getClaimedPages().firstEntry().getValue().await();
                    long capacity = buffer.getCapacity();
                    long endInPage = indexInPage + rangeLength;
                    int endIndex = Ints.saturatedCast(Math.min(capacity, endInPage));

                    List<T> list = buffer.getDataAsList();
                    List<T> subList = list.subList(indexInPage, endIndex);
                    rangeIterator = subList.iterator();

                    // rangeIterator = IteratorUtils.limit(rangeBuffer.blockingIterator(start), length);


//                        rangeIterator = rangeBuffer.getBufferAsList().subList(range.lowerEndpoint(), range.upperEndpoint())
//                                .iterator();
                }
                break;
            }
        }

        result = rangeIterator.next();
        ++readsFromCurrentRange;
        return result;
    }

    @Override
    public void close() {
        pageRange.close();
    }
}