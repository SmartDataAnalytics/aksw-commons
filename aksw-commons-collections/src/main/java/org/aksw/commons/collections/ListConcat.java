package org.aksw.commons.collections;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ListConcat<T>
	extends AbstractList<T>
{
	protected Collection<List<T>> lists;

	public ListConcat(Collection<List<T>> lists) {
		super();
		this.lists = lists;
	}

	@Override
	public T get(int index) {
		int tmp = index;
		Iterator<List<T>> it = lists.iterator();

		while(it.hasNext()) {
			List<T> list = it.next();
			int n = list.size();
			if(tmp < n) {
				T result = list.get(tmp);
				return result;
			}

			tmp -= n;
		}

		throw new IndexOutOfBoundsException("Requested: " + index + ", Size: " + size());
	}

	@Override
	public int size() {
		int result = lists.stream().mapToInt(Collection::size).sum();
		return result;
	}



}
