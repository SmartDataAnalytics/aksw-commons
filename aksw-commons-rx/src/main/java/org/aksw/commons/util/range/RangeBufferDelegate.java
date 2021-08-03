package org.aksw.commons.util.range;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;

//public interface RangeBufferDelegate<T>
//	extends RangeBuffer<T>
//{
//	RangeBuffer<T> getDelegate();
//
//	@Override
//	default Iterator<T> blockingIterator(int offset) {
//		return getDelegate().blockingIterator(offset);
//	}
//
//	@Override
//	default int getKnownSize() {
//		return getDelegate().getKnownSize();
//	}
//
//	@Override
//	default RangeMap<Integer, List<Throwable>> getFailedRanges() {
//		return getDelegate().getFailedRanges();
//	}
//
//	@Override
//	default RangeSet<Integer> getLoadedRanges() {
//		return getDelegate().getLoadedRanges();
//	}
//
//	@Override
//	default int getCapacity() {
//		return getDelegate().getCapacity();
//	}
//
//	@Override
//	public void put(int offset, T item) {
//		return getDelegate()
//	}
//
//	@Override
//	public void putAll(int offset, Object arrayWithItemsOfTypeT) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void putAll(int pageOffset, Object arrayWithItemsOfTypeT, int arrOffset, int arrLength) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public long getGeneration() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public void setKnownSize(int size) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public int knownSize() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public ReadWriteLock getReadWriteLock() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
