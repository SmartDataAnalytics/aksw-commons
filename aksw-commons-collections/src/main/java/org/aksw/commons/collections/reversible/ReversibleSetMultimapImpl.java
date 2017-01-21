package org.aksw.commons.collections.reversible;

import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.SetMultimap;

/**
 * This implementation acts as a slave to the ReversibleMap:
 * Insert methods are delegated to ReversibleMap.
 *
 */
public class ReversibleSetMultimapImpl<K, V>
	extends AbstractSetMultimap<K, V>
	implements ReversibleSetMultimap<K, V>
{
	protected ReversibleMap<V, K> reverse;

	protected SetMultimap<K, V> forward;


	public ReversibleSetMultimapImpl(ReversibleMap<V, K> reverse, SetMultimap<K, V> forward) {
		this.reverse = reverse;
		this.forward = forward;
	}


	@Override
	public ReversibleMap<V, K> reverse() {
		return reverse;
	}

	@Override
	public boolean put(K key, V value) {
		K tmp = reverse.put(value, key);
		boolean change = tmp == key;
		return change;
	}

	@Override
	public Set<Entry<K, V>> entries() {
		return forward.entries();
	}

	@Override
	public Set<V> get(K key) {
		Set<V> result = forward.get(key);
		return result;
	}

	@Override
	public boolean remove(Object key, Object value) {
		boolean result = forward.containsEntry(key, value);
		if(result) {
			reverse.remove(value);
		}
		return result;
	}

	@Override
	public Set<V> removeAll(Object key) {
		Set<V> result = new LinkedHashSet<>(forward.get((K)key));
		result.forEach(reverse::remove);
		return result;
	}

	@Override
	public void clear() {
		reverse.clear();
	}

	@Override
	public int hashCode() {
		return forward.hashCode();
	}

	@Override
	public boolean equals(Object arg) {
		return forward.equals(arg);
	}
//
//	@Override
//	protected Multimap<K, V> delegate() {
//		return forward;
//	}
}