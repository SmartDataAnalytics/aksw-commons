package org.aksw.commons.collections;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * Collection view of an underlying iterable.
 * If the provided iterable is a collection, .size() will delegate to it
 * 
 * @author raven
 *
 * @param <T>
 */
public class CollectionFromIterable<T>
	extends AbstractCollection<T>
{
	protected Iterable<T> iterable;
	
	public CollectionFromIterable(Iterable<T> iterable) {
		super();
		this.iterable = iterable;
	}

	@Override
	public Iterator<T> iterator() {
		Iterator<T> result = iterable.iterator();
		return result;
	}

	@Override
	public int size() {
		int result = Iterators.size(iterator());
		return result;
	}
	
	@Override
	public String toString() {
		String result = Iterables.toString(iterable);
		return result;
	}
	
	public static <T> Collection<T> wrap(Iterable<T> iterable) {
		Collection<T> result = iterable instanceof Collection
				? (Collection<T>)iterable
				: new CollectionFromIterable<>(iterable); 

		return result;
	}

	public static <T> CollectionFromIterable<T> create(Iterable<T> iterable) {
		return new CollectionFromIterable<>(iterable);
	}
}
