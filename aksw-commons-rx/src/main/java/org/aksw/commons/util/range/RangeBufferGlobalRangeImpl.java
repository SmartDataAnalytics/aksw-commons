package org.aksw.commons.util.range;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.aksw.commons.rx.cache.range.SliceMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.math.LongMath;
import com.google.common.primitives.Ints;


public class RangeBufferGlobalRangeImpl<T>
    implements RangeBuffer<T>
{
    private static final Logger logger = LoggerFactory.getLogger(RangeBufferGlobalRangeImpl.class);

    protected SliceMetaData metaData;
    // protected ReadWriteLock metaDataReadWriteLock;

    // The global offset for the given buffer
    protected long globalOffset;
    protected T[] buffer;

    /**
     * Used for dirty checking; incremented on every put
     * -even if the same items were re-added. Note that put acquires the write lock.
     */
    protected volatile long generation = 0;

    // protected transient List<T> listView;
    protected transient ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    protected transient Condition hasData = readWriteLock.writeLock().newCondition();


    /** Clones the object. Internally acquires the read lock. */
//    @Override
//    protected RangeBufferStandaloneImpl<T> clone() throws CloneNotSupportedException {
//        Lock readLock = getReadWriteLock().readLock();
//        readLock.lock();
//        try {
//            RangeSet<Integer> loadedRangesClone = TreeRangeSet.create();
//            loadedRangesClone.addAll(loadedRanges);
//
//            RangeMap<Integer, List<Throwable>> failedRangesClone = TreeRangeMap.create();
//            failedRangesClone.putAll(failedRangesClone);
//
//
//            T[] bufferClone = buffer.clone();
//            return new RangeBufferStandaloneImpl<>(bufferClone, loadedRangesClone, failedRangesClone, knownSize);
//        } finally {
//            readLock.unlock();
//        }
//    }

    public Condition getHasDataCondition() {
        return hasData;
    }

    public T[] getBuffer() {
        return buffer;
    }

    public List<T> getBufferAsList() {
        // return listView;
        return Arrays.asList(buffer);
    }

    protected RangeBufferGlobalRangeImpl() {
        super();
    }

//    @SuppressWarnings("unchecked")
//    public RangeBufferGlobalRangeImpl(int buffer) {
//        this((T[])new Object[size], TreeRangeSet.create(), TreeRangeMap.create(), -1);
//    }

    public RangeBufferGlobalRangeImpl(
            long globalOffset,
            T[] buffer,
            SliceMetaData metaData)
    {
        super();
        this.globalOffset = globalOffset;
        this.buffer = buffer;
        this.metaData = metaData;
    }

    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    @Override
    public Iterator<T> blockingIterator(long offsetInBuffer) {
        return new RangeBufferIterator<>(this, offsetInBuffer);
    }

    @Override
    public RangeSet<Long> getLoadedRanges() {
        // TODO May the global range to the local one of this buffer
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public RangeMap<Long, List<Throwable>> getFailedRanges() {
        // TODO May the global range to the local one of this buffer
        throw new UnsupportedOperationException("not yet implemented");
    }


    @Override
    public long getCapacity() {
        return buffer.length; // listView.size();
    }

    //public void add(T item) {
    //	int idx = numLoadedItems.getAndIncrement();
    //	items[idx] = item;
    //	notifyAll();
    //}

    @Override
    public void put(int offset, Object item) {
        putAll(offset, new Object[]{ item });
    }

    @Override
    public void putAll(int offset, Object arrayWithItemsOfTypeT) {
        putAll(offset, arrayWithItemsOfTypeT, 0, Array.getLength(arrayWithItemsOfTypeT));
    }

    @Override
    public void putAll(int offsetInBuffer, Object arrayWithItemsOfTypeT, int arrOffset, int arrLength) {
        Lock contentWriteLock = readWriteLock.writeLock();
        contentWriteLock.lock();

        try {
            RangeSet<Long> loadedRanges = metaData.getLoadedRanges();

            // Prevented overwrites do not increment the generation
            boolean preventOverwriteOfLoadedRanges = true;
            long offset = globalOffset + offsetInBuffer;
            Range<Long> writeRange = Range.closedOpen(offset, LongMath.saturatedAdd(offset, arrLength))
                    .canonical(DiscreteDomain.longs());
            if (preventOverwriteOfLoadedRanges) {
                if (loadedRanges.encloses(writeRange)) {
                    return;
                }
            }

            System.arraycopy(arrayWithItemsOfTypeT, arrOffset, buffer, offsetInBuffer, arrLength);
            Lock metaDataWriteLock = metaData.getReadWriteLock().writeLock();
            metaDataWriteLock.lock();
            try {
                loadedRanges.add(writeRange);
            } finally {
                metaDataWriteLock.unlock();
            }

            ++generation;
            logger.debug("PUT " + offsetInBuffer + ":" + arrLength + ": " + loadedRanges + " Generation is now " + generation);

            // Upon reaching the capacity then set known size to capacity
    //        if (arrOffset + arrLength >= buffer.length) {
    //        	knownSize = buffer.length;
    //        }

            hasData.signalAll();
        } finally {
            contentWriteLock.unlock();
        }
    }

    @Override
    public long getGeneration() {
        return generation;
    }

    @Override
    public long getKnownSize() {
        long size = metaData.getKnownSize();
        int result = size < 0 ? -1 : Math.min(buffer.length, Ints.saturatedCast(size - globalOffset));
        return result;
    }
}
