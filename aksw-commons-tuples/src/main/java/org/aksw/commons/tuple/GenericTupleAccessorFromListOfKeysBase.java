package org.aksw.commons.tuple;

import java.util.List;

public abstract class GenericTupleAccessorFromListOfKeysBase<D, C, K>
	implements GenericTupleAccessor<D, C, K>
{
	protected List<K> keys;

	public GenericTupleAccessorFromListOfKeysBase(List<K> keys) {
		super();
		this.keys = keys;
	}

	@Override
	public int getDimension() {
		return keys.size();
	}

	@Override
	public K keyAtOrdinal(int index) {
		return keys.get(index);
	}
	
	/**
	 * Search for the ordinal of the key in the underlying list in O(n).
	 */
	@Override
	public int ordinalOfKey(K key) {
		return keys.indexOf(key);
	}
	
	@Override
	public C get(D tupleLike, int componentIdx) {
		K key = keys.get(componentIdx);
		C result = get(tupleLike, key);
		return result;
	}
}
