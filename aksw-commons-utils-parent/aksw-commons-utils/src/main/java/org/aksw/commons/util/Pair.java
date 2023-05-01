package org.aksw.commons.util;

import java.util.Map;

/**
 * A pair class based on Map.Entry.
 *
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 */
public class Pair<K, V>
	implements Map.Entry<K, V>
{
	private final K key;
	private final V value;

	public static <K, V> Pair<K, V> create(K key, V value)
	{
		return new Pair<K, V>(key, value);
	}

	public Pair(K key, V value)
	{
		this.key = key;
		this.value = value;
	}

	public K getKey()
	{
		return key;
	}

	public V getValue()
	{
		return value;
	}

	public String toString()
	{
		return "(" + key + ", " + value + ")";
	}

	@Override
	public V setValue(V arg0)
	{
		throw new UnsupportedOperationException();
	}


	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		Pair<K, V> other = (Pair<K, V>) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
