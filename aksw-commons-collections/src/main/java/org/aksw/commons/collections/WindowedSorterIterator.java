package org.aksw.commons.collections;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

public class WindowedSorterIterator<T>
	extends SinglePrefetchIterator<T>
	implements IClosableIterator<T>
{
	private NavigableSet<T> buffer;
	private int maxBufferSize;
	private Iterator<T> it;
	
	public static <T> IClosableIterator<T> wrap(Iterator<T> it, int maxBufferSize, Comparator<T> comparator) {
		return new WindowedSorterIterator<T>(it, maxBufferSize, comparator);
	}
	
	public WindowedSorterIterator(Iterator<T> it, int maxBufferSize, Comparator<T> comparator) 
	{
		this.buffer = new TreeSet<T>(comparator);
		this.it = it;
		this.maxBufferSize = maxBufferSize;
	}
	
	@Override
	protected T prefetch() throws Exception
	{
		while(buffer.size() < maxBufferSize && it.hasNext()) {
			buffer.add(it.next());
		}

		return buffer.isEmpty() ? finish() : buffer.pollFirst();
	}
	
	@Override
	public void close()
	{
		if(it != null && it instanceof IClosable) {
			((IClosable)it).close();
		}
	}
}