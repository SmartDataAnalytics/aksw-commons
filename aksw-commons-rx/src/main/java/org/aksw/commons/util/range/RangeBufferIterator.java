package org.aksw.commons.util.range;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;

/**
 * An iterator over a range buffer that blocks if items are not loaded.
 *
 * @author raven
 *
 * @param <T>
 */
public class RangeBufferIterator<T>
    extends AbstractIterator<T>
{
    protected RangeBufferImpl<T> rangeBuffer;
    protected int currentIndex;

    /** Iterator over a range in the page starting at currentOffset */
    protected Iterator<T> rangeIterator = null;

    /** Number of items read from rangeIterator */
    protected int readsFromCurrentRange = 0;

    public RangeBufferIterator(RangeBufferImpl<T> page, int currentIndex) {
        super();
        this.rangeBuffer = page;
        this.currentIndex = currentIndex;
    }


    @Override
    protected T computeNext() {
        T result;

        RangeMap<Integer, List<Throwable>> loadedRanges = rangeBuffer.getLoadedRanges();

        while (rangeIterator == null || !rangeIterator.hasNext()) {
            currentIndex += readsFromCurrentRange;
            readsFromCurrentRange = 0;

            Entry<Range<Integer>, List<Throwable>> entry = null;
            Lock readLock = rangeBuffer.getReadWriteLock().readLock();

            readLock.lock();
            try {
                // If the index is outside of the known size then abort
                int knownSize = rangeBuffer.getKnownSize();
                if (knownSize >= 0 && currentIndex >= knownSize) {
                    return endOfData();
                } else {
                    entry = loadedRanges.getEntry(currentIndex);

                    if (entry == null) {
                        // Wait for data to become available
                        // Solution based on https://stackoverflow.com/questions/13088363/how-to-wait-for-data-with-reentrantreadwritelock
                        Lock writeLock = rangeBuffer.getReadWriteLock().writeLock();
                        readLock.unlock();
                        writeLock.lock();
                        try {
                            while ((entry = loadedRanges.getEntry(currentIndex)) == null) {
                                try {
                                    rangeBuffer.getHasDataCondition().await();
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

            if (entry != null) {
                List<Throwable> throwable = entry.getValue();
                if (!throwable.isEmpty()) {
                    throw new RuntimeException("Attempt to read a range of data marked with an error",
                            throwable.get(0));
                }

                Range<Integer> range = entry.getKey();
                rangeIterator = rangeBuffer.getBufferAsList().subList(range.lowerEndpoint(), range.upperEndpoint())
                        .iterator();
                break;
            }
        }

        result = rangeIterator.next();
        ++readsFromCurrentRange;
        return result;
    }
}