package org.aksw.commons.collections;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

public class SetUtils {
    public static <T> Set<T> asSet(Iterable<T> c) {
        return (c instanceof Set) ? (Set<T>) c : Sets.newHashSet(c);
    }
    

    /**
     * Short hand for
     * Set<T> result = source.stream().map(fn).collect(Collectors.toSet())
     * 
     * Maps a set of keys to a corresponding set of values via a given map
     * TODO Probably this method can be replaced by something from guava
     *
     * @param set
     * @param map
     * @return
     */
    public static <K, V> Set<V> mapSet(Set<K> set, Map<K, V> map) {
        Set<V> result = new HashSet<V>();
        for(K item : set) {
            V v = map.get(item);
            result.add(v);
        }

        return result;
    }
}
