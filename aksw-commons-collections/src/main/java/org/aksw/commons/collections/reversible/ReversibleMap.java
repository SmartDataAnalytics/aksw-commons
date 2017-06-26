package org.aksw.commons.collections.reversible;

import java.util.Map;

public interface ReversibleMap<K, V>
	extends Map<K, V>
{
	ReversibleSetMultimap<V, K> reverse();
}