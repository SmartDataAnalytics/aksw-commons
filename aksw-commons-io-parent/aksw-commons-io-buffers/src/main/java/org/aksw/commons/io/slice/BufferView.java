package org.aksw.commons.io.slice;

import java.util.concurrent.locks.ReadWriteLock;

import org.aksw.commons.io.buffer.range.RangeBuffer;

public interface BufferView<A> {
    RangeBuffer<A> getRangeBuffer();
    // ReadWriteLock getReadWriteLock();
    long getGeneration();

//    default long getCapacity() {
//        long result = getRangeBuffer().getCapacity();
//        return result;
//    }

    ReadWriteLock getReadWriteLock();
//	protected RangeBuffer rangeBuffer;
//	protected ReadWriteLock readWriteLock;
//	protected long generation;
}
