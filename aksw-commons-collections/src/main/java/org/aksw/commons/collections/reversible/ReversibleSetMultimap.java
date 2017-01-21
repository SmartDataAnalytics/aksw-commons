package org.aksw.commons.collections.reversible;

import com.google.common.collect.SetMultimap;

/**
 * Multimap in which a key can only map to a unique value.
 *
 * As a consequence, putting an entry with a value 'v' will remove
 * any prior entry with the same value.
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public interface ReversibleSetMultimap<K, V>
	extends SetMultimap<K, V>
{
	ReversibleMap<V, K> reverse();
}