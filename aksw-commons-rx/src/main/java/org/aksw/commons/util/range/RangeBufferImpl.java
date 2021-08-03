package org.aksw.commons.util.range;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;

/**
 * A list where ranges can be marked as 'loaded'.
 *
 * Access to non-loaded items block until they become loaded.
 * Producers are not managed by this class and must therefore be managed
 * externally.
 *
 * Changes to the set of loaded ranges or the known size synchronize on 'this'.
 *
 *
 * @author raven
 *
 * @param <T>
 */
public class RangeBufferImpl<T>
    implements RangeBuffer<T>, Serializable
{
    private static final Logger logger = LoggerFactory.getLogger(RangeBufferIterator.class);


    private static final long serialVersionUID = 1L;


    protected T[] buffer;

    /**
     * If the value is null then the range is considered as successfully loaded.
     * If a throwable is present then there was an error processing the range
     */
    protected RangeSet<Integer> loadedRanges;
    protected RangeMap<Integer, List<Throwable>> failedRanges;
    protected volatile int knownSize;

    /**
     * Used for dirty checking; incremented on every put
     * -even if the same items were re-added. Note that put acquires the write lock.
     */
    protected volatile long generation = 0;

    // protected transient List<T> listView;
    protected transient ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    protected transient Condition hasData = readWriteLock.writeLock().newCondition();


    /** Clones the object. Internally acquires the read lock. */
    @Override
    protected RangeBufferImpl<T> clone() throws CloneNotSupportedException {
        Lock readLock = getReadWriteLock().readLock();
        readLock.lock();
        try {
            RangeSet<Integer> loadedRangesClone = TreeRangeSet.create();
            loadedRangesClone.addAll(loadedRanges);

            RangeMap<Integer, List<Throwable>> failedRangesClone = TreeRangeMap.create();
            failedRangesClone.putAll(failedRangesClone);


            T[] bufferClone = buffer.clone();
            return new RangeBufferImpl<>(bufferClone, loadedRangesClone, failedRangesClone, knownSize);
        } finally {
            readLock.unlock();
        }
    }

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

    protected RangeBufferImpl() {
        super();
    }

    @SuppressWarnings("unchecked")
    public RangeBufferImpl(int size) {
        this((T[])new Object[size], TreeRangeSet.create(), TreeRangeMap.create(), -1);
    }

    public RangeBufferImpl(
            T[] buffer,
            RangeSet<Integer> loadedRanges,
            RangeMap<Integer, List<Throwable>> failedRanges,
            int knownSize)
    {
        super();
        this.buffer = buffer;
        this.loadedRanges = loadedRanges;
        this.failedRanges = failedRanges;
        this.knownSize = knownSize;

       postConstruct();
    }

    // @PostConstruct
    protected void postConstruct() {
        //  this.listView = Arrays.asList(buffer);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            postConstruct();
    }

    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    @Override
    public Iterator<T> blockingIterator(int offset) {
        return new RangeBufferIterator<>(this, offset);
    }

    /** -1 if unknown */
    @Override
    public int getKnownSize() {
        return knownSize;
    }

    @Override
    public RangeMap<Integer, List<Throwable>> getFailedRanges() {
        return failedRanges;
    }

    @Override
    public RangeSet<Integer> getLoadedRanges() {
        return loadedRanges;
    }


    @Override
    public int getCapacity() {
        return buffer.length; // listView.size();
    }

//	public void add(T item) {
//		int idx = numLoadedItems.getAndIncrement();
//		items[idx] = item;
//		notifyAll();
//	}

    @Override
    public void put(int offset, Object item) {
        putAll(offset, new Object[]{ item });
    }

    @Override
    public void putAll(int offset, Object arrayWithItemsOfTypeT) {
        putAll(offset, arrayWithItemsOfTypeT, 0, Array.getLength(arrayWithItemsOfTypeT));
    }

    @Override
    public void putAll(int pageOffset, Object arrayWithItemsOfTypeT, int arrOffset, int arrLength) {
        Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();

        try {
            System.arraycopy(arrayWithItemsOfTypeT, arrOffset, buffer, pageOffset, arrLength);
            loadedRanges.add(
                    Range.closedOpen(pageOffset, pageOffset + arrLength).canonical(DiscreteDomain.integers()));

            ++generation;
            logger.debug("PUT " + pageOffset + ":" + arrLength + ": " + loadedRanges + " Generation is now " + generation);
        } finally {
            hasData.signalAll();
            writeLock.unlock();
        }
    }

    @Override
    public long getGeneration() {
        return generation;
    }

    /** Sets the known size thereby synchronizing on 'this' */
    @Override
    public void setKnownSize(int size) {
        if (knownSize < 0) {
            Lock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                this.knownSize = size;
                ++this.generation;
            } finally {
                hasData.signalAll();
                writeLock.unlock();
            }
        } else {
            throw new IllegalStateException("Known size has already been set");
        }
    }

    @Override
    public String toString() {
        return "RangeBufferImpl [capacity=" + buffer.length + ", knownSize=" + knownSize + ", loadedRanges="
                + loadedRanges + ", failedRanges=" + failedRanges + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.deepHashCode(buffer);
        result = prime * result + ((failedRanges == null) ? 0 : failedRanges.hashCode());
        result = prime * result + knownSize;
        result = prime * result + ((loadedRanges == null) ? 0 : loadedRanges.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RangeBufferImpl other = (RangeBufferImpl) obj;
        if (!Arrays.deepEquals(buffer, other.buffer))
            return false;
        if (failedRanges == null) {
            if (other.failedRanges != null)
                return false;
        } else if (!failedRanges.equals(other.failedRanges))
            return false;
        if (knownSize != other.knownSize)
            return false;
        if (loadedRanges == null) {
            if (other.loadedRanges != null)
                return false;
        } else if (!loadedRanges.equals(other.loadedRanges))
            return false;
        return true;
    }






//	public static <T> Page<T> create(int pageSize) {
//		return new Page<T>((T[])new Object[pageSize], 0);
//	}
}
