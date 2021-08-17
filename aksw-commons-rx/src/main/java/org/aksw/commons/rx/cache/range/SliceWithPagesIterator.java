package org.aksw.commons.rx.cache.range;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.aksw.commons.collections.IteratorUtils;
import org.aksw.commons.util.ref.RefFuture;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;


/**
 * An iterator over a range buffer that blocks if items are not loaded.
 *
 * @author raven
 *
 * @param <T>
 */
public class SliceWithPagesIterator<T>
    extends AbstractIterator<T>
    implements AutoCloseable
{
    protected SliceWithPages<T> rangeBuffer;
    protected long currentIndex;

    /** Iterator over a range in the page starting at currentOffset */
    protected Iterator<T> rangeIterator = null;

    /** Number of items read from rangeIterator */
    protected int readsFromCurrentRange = 0;

    protected PageRange<T> pageRange;


    public SliceWithPagesIterator(SliceWithPages<T> page, long currentIndex) {
        super();
        this.rangeBuffer = page;
        this.currentIndex = currentIndex;
        this.pageRange = page.newPageRange();
    }


    @Override
    protected T computeNext() {
        T result;

        while (rangeIterator == null || !rangeIterator.hasNext()) {
            currentIndex += readsFromCurrentRange;
            readsFromCurrentRange = 0;

            try (RefFuture<SliceMetaData> ref = rangeBuffer.getMetaData()) {
                SliceMetaData metaData = ref.await();

                Lock readLock = metaData.getReadWriteLock().readLock();
                readLock.lock();

                try {
                    RangeSet<Long> loadedRanges = metaData.getLoadedRanges();
                    RangeMap<Long, List<Throwable>> failedRanges = metaData.getFailedRanges();

                    Range<Long> entry = null;
                    List<Throwable> failures = null;

                    try {
                        // If the index is outside of the known size then abort
                        // long knownSize = metaData.getSize();
                        long maximumSize = metaData.getMaximumKnownSize();
                        if (currentIndex >= maximumSize) {
                            return endOfData();
                        } else {

                            // rangeBuffer.getFailedRanges().getEntry(currentIndex);

                            failures = failedRanges.get(currentIndex); // .getEntry(currentIndex);
                            entry = loadedRanges.rangeContaining(currentIndex);

                            if (entry == null && failures == null) {
                                // Wait for data to become available
                                // Solution based on https://stackoverflow.com/questions/13088363/how-to-wait-for-data-with-reentrantreadwritelock

                                Lock writeLock = metaData.getReadWriteLock().writeLock();
                                readLock.unlock();
                                writeLock.lock();

                                try {
                                    long knownSize;
                                    while ((entry = loadedRanges.rangeContaining(currentIndex)) == null &&
                                            ((knownSize = metaData.getSize()) < 0 || currentIndex < knownSize)) {
                                        try {
                                            metaData.getHasDataCondition().await();
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                    readLock.lock();
                                } finally {
                                    writeLock.unlock();
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
                        return endOfData();
                    } else {
                        Range<Long> range = Range.atLeast(currentIndex).intersection(entry); //  entry; //.getKey();

                        long start = range.lowerEndpoint();
                        long end = range.upperEndpoint();
                        long length = end - start;

                        pageRange.claimByOffsetRange(start, end);


                        rangeIterator = IteratorUtils.limit(rangeBuffer.blockingIterator(start), length);


//                        rangeIterator = rangeBuffer.getBufferAsList().subList(range.lowerEndpoint(), range.upperEndpoint())
//                                .iterator();
                    }
                    break;
                } finally {
                    readLock.unlock();
                }
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