package org.aksw.commons.collections.reversible;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;

public abstract class AbstractSetMultimap<K, V>
	implements SetMultimap<K, V>
{
	@Override
	public int size() {
		int result = this.entries().size();
		return result;
	}

	@Override
	public boolean isEmpty() {
		boolean result = this.entries().isEmpty();
		return result;
	}

	@Override
	public boolean containsKey(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsEntry(Object key, Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean put(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object key, Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean putAll(K key, Iterable<? extends V> values) {
		values.forEach(value -> put(key, value));
		return true;
	}

	@Override
	public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
		multimap.entries().forEach(e -> put(e.getKey(), e.getValue()));
		return true;
	}

	@Override
	public Set<V> replaceValues(K key, Iterable<? extends V> values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<V> removeAll(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<V> get(K key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<K> keySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Multiset<K> keys() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<V> values() {
		throw new UnsupportedOperationException();
	}

//	@Override
//	public Collection<Entry<K, V>> entries() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public Map<K, Collection<V>> asMap() {
		throw new UnsupportedOperationException();
	}
}