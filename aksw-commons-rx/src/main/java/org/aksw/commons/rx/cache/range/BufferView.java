package org.aksw.commons.rx.cache.range;

import java.util.concurrent.locks.ReadWriteLock;

public interface BufferView<A> {
	RangeBuffer<A> getRangeBuffer();
	ReadWriteLock getReadWriteLock();
	long getGeneration();
	
//	protected RangeBuffer rangeBuffer;
//	protected ReadWriteLock readWriteLock;
//	protected long generation;
}
