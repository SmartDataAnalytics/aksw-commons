package org.aksw.commons.util.range;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;

import org.aksw.commons.util.array.ArrayWritable;

import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;

public interface RangeBuffer<T>
	extends ArrayWritable
{

    /**
     * Return an iterator initialized at the given offset
     * which blocks upon accessing an index outside of the data or failure ranges.
     *
     * @param offset
     * @return
     */
    Iterator<T> blockingIterator(long offset);



    // Iterator<T> getDataIterator();


    /** The size determines the maximum valid index in the buffer; -1 if unknown */
    long getKnownSize();

    /**
     * A map of ranges to a single optional exception.
     * Note, that List is used instead of Optional because
     * the former's implementations are typically serializable
     */
    RangeMap<Long, List<Throwable>> getFailedRanges();
    RangeSet<Long> getLoadedRanges();

    long getCapacity();

//    void put(int offset, T item);
//    void putAll(int offset, Object arrayWithItemsOfTypeT);
//    void putAll(int pageOffset, Object arrayWithItemsOfTypeT, int arrOffset, int arrLength);


    /** Every call to put that modifies this object's state increments the generation */
    long getGeneration();

    /** Sets the known size. Acquires the write lock before updating the known size
     *  property and afterwards wakes up all iterators obtained via
     * {@link #blockingIterator(int)} */
    // void setKnownSize(int size);


    /**
     * Obtain this buffer's read/write lock
     *
     * @return
     */
    ReadWriteLock getReadWriteLock();
    
    
    /**
     * A condition that:
     *  (a) blocking iterators will wait for if no data is available
     *  (b) producers must signal whenever they write data
     * 
     * @return
     */
    Condition getHasDataCondition();
}
