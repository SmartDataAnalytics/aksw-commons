package org.aksw.commons.collections.tagmap;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A map-like data structure where values can be accessed via set of tags.
 *
 * The same set of tags may be associated with multiple values, hence, this is a multi map.
 *
 *
 * @author raven
 *
 */
public interface TagIndex<T, V>
    extends Collection<Entry<Set<T>, Set<V>>>
    //extends Map<Set<T>, Set<V>>
//    extends SetMultimap<Set<T>, V>
{
    //Collection<V>
    void put(Set<T> tags, V value);
    void removeAll(Set<T> tags);
    void remove(Set<T> tags, V value);

    //Iterator<Entry<Set<T>, Set<V>>> entries();

    TagIndex<T, V> getAllSubsetsOf(Set<T> tags, boolean strict);
    TagIndex<T, V> getAllSupersetsOf(Set<T> tags, boolean strict);
    TagIndex<T, V> getAllEquisetsOf(Set<T> tags);
}

