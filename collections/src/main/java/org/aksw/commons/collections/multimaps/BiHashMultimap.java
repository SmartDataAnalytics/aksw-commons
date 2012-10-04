package org.aksw.commons.collections.multimaps;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BiHashMultimap<K, V>
	implements IBiSetMultimap<K, V>
{
	private BiHashMultimap<V, K> inverse;

	private SetMultimap<K, V> forward;
	private SetMultimap<V, K> backward;	
	
	public BiHashMultimap()
	{
		this.forward = HashMultimap.create();
		this.backward = HashMultimap.create();
		
		inverse = new BiHashMultimap<V, K>(this);
		inverse.inverse = this;
	}

	/**
	 * Constructor for reverse map
	 * 
	 * @param original
	 */
	protected BiHashMultimap(BiHashMultimap<V, K> original)
	{
		this.inverse = original;
		this.forward = original.backward;
		this.backward = original.forward;
	}
	
	public BiHashMultimap<V, K> getInverse()
	{
		return inverse;
	}
	
	
	// TODO Must be wrapped
	public Map<K, Collection<V>> asMap()
	{
		return forward.asMap();
	}
	
	public void remove(K key, V value)
	{
		forward.remove(key, value);
		backward.remove(value, key);
	}

	@Override
	public boolean put(K key, V value)
	{
		boolean result = forward.put(key, value);
		backward.put(value, key);
		
		return result;
	}
	

	@Override
	public Set<V> removeAll(Object key)
	{
		for(V value : forward.get((K)key)) {
			backward.remove(value, key);
		}
		
		return forward.removeAll(key);
	}


	@Override
	public Set<V> get(Object key)
	{
		return forward.get((K) key);
	}

    public boolean containsEntry(Object key, Object value) {
        return forward.containsEntry(key, value);
    }

    public boolean containsValue(Object value) {
        return forward.containsValue(value);
    }

    public boolean containsKey(Object key) {
        return forward.containsKey(key);
    }

	@Override
	public Set<Entry<K, V>> entries()
	{
		return forward.entries();
	}

	@Override
	public void putAll(ISetMultimap<K, V> other)
	{
		for(Entry<K, V> entry : other.entries()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public String toString()
	{
		return forward.toString();
	}

    public int size() {
        return forward.size();
    }

	@Override
	public void clear()
	{
		forward.clear();
		backward.clear();
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BiHashMultimap that = (BiHashMultimap) o;

        if (forward != null ? !forward.equals(that.forward) : that.forward != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return forward != null ? forward.hashCode() : 0;
    }
}
