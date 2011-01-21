package org.aksw.commons.util.collections;

import java.util.*;

/**
 * Created by Claus Stadler
 * Date: Oct 9, 2010
 * Time: 5:42:34 PM
 *
 * A set of static methods for treating a Map<K, Set<V>> as a multimap
 * (or rather graphs, as keys may be mapped to an empty set of values)
 *
 */
public class MultiMaps
{
    public static <K, V> void putAll(Map<K, Set<V>> target, Map<? extends K, ? extends Set<? extends V>> source)
    {
        for(Map.Entry<? extends K, ? extends Set<? extends V>> entry : source.entrySet()) {
            putAll(target, entry.getKey(), entry.getValue());
        }
    }

    public static <K, V> void putAll(Map<K, Set<V>> map, K key, Collection<? extends V> vs)
    {
        Set<V> values = addKey(map, key);
        values.addAll(vs);
    }

    public static <K, V> void put(Map<K, Set<V>> map, K key, V value)
    {
        Set<V> values = addKey(map, key);
        values.add(value);
    }

    public static <K, V> boolean containsEntry(Map<K, Set<V>> map, K key, V value)
    {
        Set<V> values = map.get(key);
        return values == null ? false : values.contains(value);
    }

    public static <K, V> Collection<V> values(Map<K, Set<V>> map)
    {
        return new FlatMapView<V>(map.values());
    }

    public static <K, V> Set<V> addKey(Map<K, Set<V>> map, K key)
    {
        Set<V> values = map.get(key);
        if(values == null) {
            values = new HashSet<V>();
            map.put(key, values);
        }

        return values;
    }

    public static <K, V> void addAllKeys(Map<K, Set<V>> map, Iterable<K> keys)
    {
        for(K key : keys) {
            addKey(map, key);
        }
    }

    public static <K, V> Map<K, Set<V>> copy(Map<K, Set<V>> source)
    {
        Map<K, Set<V>> result = new HashMap<K, Set<V>>();

        putAll(result, source);

        return result;
    }

    /**
     * This method returns an empty set (Collections.emptySet) for keys that are not in the map.
     *
     * @param map
     * @param key
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Set<V> safeGet(Map<K, Set<V>> map, K key)
    {
        Set<V> values = map.get(key);
        return (values == null)
                ? Collections.<V>emptySet()
                : values;
    }
    //public static <K, V>
}