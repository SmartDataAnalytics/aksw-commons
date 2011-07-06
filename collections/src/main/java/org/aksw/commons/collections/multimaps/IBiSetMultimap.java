package org.aksw.commons.collections.multimaps;

public interface IBiSetMultimap<K, V>
	extends ISetMultimap<K, V>
{
	IBiSetMultimap<V, K> getInverse();
}