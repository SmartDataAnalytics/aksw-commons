package org.aksw.commons.collections.multimaps;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public interface ISetMultimap<K, V>
{
	Map<K, Collection<V>> asMap();
	
	boolean put(K key, V value);
	void putAll(ISetMultimap<K, V> other);
	
	Set<V> removeAll(Object key);
	
	Set<V> get(Object key);
	
	Set<Entry<K, V>> entries();

    boolean containsEntry(Object key, Object value);
    boolean containsValue(Object value);
    boolean containsKey(Object key);

	void clear();
}