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

    // protected transient List<T> listView;
    protected transient ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    protected transient Condition hasData = readWriteLock.writeLock().newCondition();


    /** Clones the object. Internally attempts to acquire the read lock. */
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
    public Iterator<T> get(int offset) {
        return new RangeBufferIterator<>(this, offset);
    }

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

            logger.debug("PUT " + pageOffset + ":" + arrLength + ": " + loadedRanges);
        } finally {
            hasData.signalAll();
            writeLock.unlock();
        }
    }

    /** Sets the known size thereby synchronizing on 'this' */
    @Override
    public void setKnownSize(int size) {
        if (knownSize < 0) {
            Lock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                this.knownSize = size;
            } finally {
                hasData.signalAll();
                writeLock.unlock();
            }
        }
    }

    /** -1 if unknown */
    @Override
    public int knownSize() {
        return knownSize;
    }

//	public static <T> Page<T> create(int pageSize) {
//		return new Page<T>((T[])new Object[pageSize], 0);
//	}
}
