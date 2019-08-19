package org.aksw.commons.collections.sets;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * In contrast to {@link SetFromCollection}, the unsafe version
 * does not handle duplicates in the backend collection.
 * Only use the unsafe version if the uniqueness of items in the backend is ensured.
 * 
 * This class simply delegates calls to iterator() and size() to the backend.
 * 
 * @author raven
 *
 */
public class SetFromCollectionUnsafe<T>
	extends AbstractSet<T>
{
	protected Collection<T> backend;
	
	public SetFromCollectionUnsafe(Collection<T> backend) {
		super();
		this.backend = backend;
	}

	@Override
	public boolean add(T e) {
		boolean result = backend.contains(e) ? false : backend.add(e);
		return result;
	}
	
	/**
	 * This method removes ALL occurrences of a given item.
	 * The backend MUST adhere to the contract that remove()
	 * returns false once there is no more change - otherwise
	 * calling this method will cause an endless loop.
	 *
	 */
	@Override
	public boolean remove(Object o) {
		boolean result = backend.remove(o);
		return result;
	}
	
	@Override
	public boolean contains(Object o) {
		boolean result = backend.contains(o);
		return result;
	}

	@Override
	public void clear() {
		backend.clear();
	}

	@Override
	public Iterator<T> iterator() {
		Iterator<T> result = backend.iterator();
		return result;
	}

	@Override
	public int size() {
		int result = backend.size();
		return result;
	}

	public static <T> Set<T> wrapIfNeeded(Collection<T> collection) {
		Set<T> result = collection instanceof Set ? (Set<T>)collection : new SetFromCollectionUnsafe<>(collection);
		return result;
	}
}
