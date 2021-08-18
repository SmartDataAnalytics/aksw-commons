package org.aksw.commons.util.range;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.aksw.commons.rx.cache.range.PutHelper;

import com.google.common.primitives.Ints;

public class BufferWithGeneration<T>
    implements PutHelper
{
    protected Object[] data;
    protected int generation;

    protected transient ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /** For serialization */
    protected BufferWithGeneration() {
    }

    public BufferWithGeneration(int size) {
        this(new Object[size], 0);
    }

    public BufferWithGeneration(Object[] data) {
        this(data, 0);
    }

    public BufferWithGeneration(Object[] data, int generation) {
        this.data = data;
        this.generation = generation;
    }

    public List<T> getDataAsList() {
        return (List<T>)Arrays.asList(data);
    }

    public long getCapacity() {
        return data.length;
    }

    public long getGeneration() {
        return generation;
    }

    public void incrementGeneration() {
        ++generation;
    }

    @Override
    public void putAll(long offsetInBuffer, Object arrayWithItemsOfTypeT, int arrOffset, int arrLength) {

//        Lock lock = readWriteLock.writeLock();
//        lock.lock();
//        try {
//
            int o = Ints.checkedCast(offsetInBuffer);

            incrementGeneration();


            System.arraycopy(arrayWithItemsOfTypeT, arrOffset, data, o, arrLength);
//        } finally {
//            lock.unlock();
//        }
    }

    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }
}
