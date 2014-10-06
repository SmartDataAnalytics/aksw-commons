package org.aksw.commons.collections.multimaps;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface ISetMultimap<K, V>
{
	Map<K, Collection<V>> asMap();
	
	boolean put(K key, V value);

	void putAll(K key, Collection<V> values);
	void putAll(ISetMultimap<K, V> other);
	
	Set<V> removeAll(Object key);
	
	Set<V> get(Object key);
	
	Set<K> keySet();
	Collection<K> keys();
	Set<Entry<K, V>> entries();

    boolean containsEntry(Object key, Object value);
    boolean containsValue(Object value);
    boolean containsKey(Object key);

    int size();

	void clear();
}