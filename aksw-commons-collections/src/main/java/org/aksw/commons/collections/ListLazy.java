package org.aksw.commons.collections;

import java.util.AbstractList;
import java.util.function.Function;

public class ListLazy<T>
	extends AbstractList<T>
{
	protected Function<Integer, T> accessor;
	protected int size;

	/**
	 * The accessor must be defined for the range [0, size)
	 *
	 * @param accessor
	 * @param size
	 */
	public ListLazy(Function<Integer, T> accessor, int size) {
		super();
		this.accessor = accessor;
		this.size = size;
	}

	@Override
	public T get(int index) {
		if(index > size) {
			throw new IndexOutOfBoundsException("Requested: " + index + ", Size: " + size);
		}
		T result = accessor.apply(index);
		return result;
	}

	@Override
	public int size() {
		return size;
	}
}
