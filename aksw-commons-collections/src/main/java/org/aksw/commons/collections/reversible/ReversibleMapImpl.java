package org.aksw.commons.collections.reversible;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;


public class ReversibleMapImpl<K, V>
	extends AbstractMap<K, V>
	implements ReversibleMap<K, V>
{
	protected ReversibleSetMultimap<V, K> reverse;

	protected Map<K, V> forward;
	protected SetMultimap<V, K> backward;

	public ReversibleMapImpl() {
		this.forward = new HashMap<>();
		this.backward = HashMultimap.create();

		this.reverse = new ReversibleSetMultimapImpl<>(this, this.backward);
	}

	@Override
	public V put(K key, V value) {
		V result = forward.get(key);
		backward.remove(result, key);

		forward.put(key, value);
		backward.put(value, key);

		return result;
	}

	@Override
	public V remove(Object key) {
		V result = forward.remove(key);
		backward.remove(result, key);
		return result;
	}

	@Override
	public void clear() {
		forward.clear();
		backward.clear();
	}

	@Override
	public V get(Object key) {
		return forward.get(key);
	}

	@Override
	public boolean containsKey(Object key) {
		return forward.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return backward.containsKey(value);
	}

	@Override
	public ReversibleSetMultimap<V, K> reverse() {
		return reverse;
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return forward.entrySet();
	}
}
