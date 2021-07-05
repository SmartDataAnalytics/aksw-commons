package org.aksw.commons.util.range;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

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
    private static final long serialVersionUID = 1L;

    // Dummy throwable because range map does not allow null values
    // and optional is not serializable...
    public static final Throwable OK = new Throwable();


    protected T[] buffer;

    /**
     * If the value is null then the range is considered as successfully loaded.
     * If a throwable is present then there was an error processing the range
     */
    protected RangeMap<Integer, List<Throwable>> loadedRanges;
    protected volatile int knownSize;

    protected transient List<T> listView;
    protected transient ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    protected RangeBufferImpl() {
        super();
    }


    public T[] getBuffer() {
        return buffer;
    }

    public List<T> getBufferAsList() {
        return listView;
    }

    @SuppressWarnings("unchecked")
    public RangeBufferImpl(int size) {
        this((T[])new Object[size], TreeRangeMap.create(), -1);
    }

    public RangeBufferImpl(T[] buffer, RangeMap<Integer, List<Throwable>> loadedRanges, int knownSize) {
        super();
        this.buffer = buffer;
        this.loadedRanges = loadedRanges;
        this.knownSize = knownSize;

       postConstruct(); // Is that needed?
    }

    // @PostConstruct
    protected void postConstruct() {
        this.listView = Arrays.asList(buffer);

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
    public RangeMap<Integer, List<Throwable>> getLoadedRanges() {
        return loadedRanges;
    }


    @Override
    public int getCapacity() {
        return listView.size();
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
            loadedRanges.put(Range.closedOpen(pageOffset, pageOffset + arrLength), Collections.emptyList());
        } finally {
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
