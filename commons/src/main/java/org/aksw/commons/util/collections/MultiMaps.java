package org.aksw.commons.util.collections;

import scala.collection.immutable.*;

import java.lang.Iterable;
import java.util.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        for(K key :  keys) {
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
     * Creates a new map that is the reverse of the source
     * 
     * @param source
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<V, Set<K>> reverse(Map<K, Set<V>> source)
    {
        Map<V, Set<K>> result = new HashMap<V, Set<K>>();

        for(Map.Entry<K, Set<V>> entry : source.entrySet()) {
            for(V value : entry.getValue()) {
                put(result, value, entry.getKey());
            }
        }

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
    public static <K, V> Set<V> safeGet(Map<K, Set<V>> map, Object key)
    {
        Set<V> values = map.get(key);
        return (values == null)
                ? Collections.<V>emptySet()
                : values;
    }

    public static <T> Set<T> transitiveGet(Map<T, Set<T>> map, Object key)
    {
        Set<T> result = new HashSet<T>(safeGet(map, key));
        Set<T> open = new HashSet<T>(result);
        Set<T> next = new HashSet<T>();

        do {
            for(T a : open) {
                for(T b : safeGet(map, a)) {
                    if(result.contains(b))
                        continue;

                    next.add(b);
                }
            }

            open.clear();
            result.addAll(next);

            Set<T> tmp = next;
            next = open;
            open = tmp;

        } while(!open.isEmpty());

        return result;
    }

    public static <T> Map<T, Set<T>> transitiveClosure(Map<T, Set<T>> map)
    {
        return transitiveClosure(map, false);
    }

    public static <T> Map<T, Set<T>> transitiveClosure(Map<T, Set<T>> map, boolean inPlace)
    {
        return transitiveClosureInPlace(inPlace ? map : copy(map));
    }

    public static <T> Map<T, Set<T>> transitiveClosureInPlace(Map<T, Set<T>> map)
    {
        return transitiveClosureInPlace(map, reverse(map));
    }

    public static <T> Map<T, Set<T>> transitiveClosureInPlace(Map<T, Set<T>> map, Map<T, Set<T>> rev)
    {
        Map<T, Set<T>> open = map;
        Map<T, Set<T>> next = new HashMap<T, Set<T>>();

        do {
            // Check if any edge leading to an open edge would create a new edge
            for(Map.Entry<T, Set<T>> edgeB : open.entrySet()) {

                for(T nodeA : safeGet(rev, edgeB.getKey())) {
                    for(T nodeC : edgeB.getValue()) {

                        if(!containsEntry(map, nodeA, nodeC)) {

                            // We would need the following statement if
                            // put(next, nodeA, nodeC) allowed duplicates
                            // put(map, nodeA, nodeC);
                            put(rev, nodeC, nodeA);
                            put(next, nodeA, nodeC);
                        }
                    }
                }
            }

            putAll(map, next);

            if(open == map) {
                if(next.isEmpty()) {
                    break;
                } else {
                    open = new HashMap<T, Set<T>>();
                }
            } else {
                open.clear();
            }

            Map<T, Set<T>> tmp = next;
            next = open;
            open = tmp;

        } while (!open.isEmpty());

        return map;
    }

    //public static <K, V>
}