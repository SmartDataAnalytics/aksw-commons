package org.aksw.commons.util.range;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import com.google.common.collect.RangeMap;

public interface RangeBuffer<T> {

    Iterator<T> get(int offset);

    int getKnownSize();

    RangeMap<Integer, List<Throwable>> getLoadedRanges();

    int getCapacity();

    void put(int offset, T item);
    void putAll(int offset, Object arrayWithItemsOfTypeT);
    void putAll(int pageOffset, Object arrayWithItemsOfTypeT, int arrOffset, int arrLength);

    /** Sets the known size thereby synchronizing on 'this' */
    void setKnownSize(int size);

    /** -1 if unknown */
    int knownSize();

    ReadWriteLock getReadWriteLock();


}