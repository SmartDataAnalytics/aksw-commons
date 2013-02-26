package org.aksw.commons.collections;

import com.google.common.collect.Iterables;

import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * A wrapper which makes an iterable appear as a collection.
 *
 * @author Claus Stadler
 */
public class IterableCollection<T>
	extends AbstractCollection<T>
{
	private Iterable<T> iterable;

	public static <T> IterableCollection<T> wrap(Iterable<T> iterable)
	{
		return new IterableCollection<T>(iterable);
	}

	public IterableCollection(Iterable<T> iterable)
	{
		this.iterable = iterable;
	}

	@Override
	public Iterator<T> iterator()
	{
		return iterable.iterator();
	}

	@Override
	public int size()
	{
		return Iterables.size(iterable);
	}

}
