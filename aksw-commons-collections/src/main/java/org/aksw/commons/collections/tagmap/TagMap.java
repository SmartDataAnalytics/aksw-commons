package org.aksw.commons.collections.tagmap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A map where each key is associated with a set of items that act as tags.
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public interface TagMap<K, V>
    extends Map<K, Set<V>>
{
    /**
     *
     * @param tags
     * @param strict If true, the result contains only the strict subsets of the given tags
     * @return
     */
    TagMap<K, V> getAllSubsetsOf(Collection<?> tags, boolean strict);

    /**
     *
     * @param tags
     * @param strict If true, the result contains only the strict supersets of the given tags
     * @return
     */
    TagMap<K, V> getAllSupersetsOf(Collection<?> tags, boolean strict);


    /**
     * equisets: short term for equivalent sets
     *
     * @param tags
     * @return
     */
    TagMap<K, V> getAllEquisetsOf(Collection<?> tags);
}
