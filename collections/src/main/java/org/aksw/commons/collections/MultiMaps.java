package org.aksw.commons.collections;


import com.google.common.collect.Sets;

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
    public static <K, V> Set<V> safeGet(Map<K, ? extends Collection<V>> map, Object key)
    {
        Collection<V> values = map.get(key);
        return (values == null)
                ? Collections.<V>emptySet()
                : CollectionUtils.asSet(values);
    }

    /**
     * Returns the set of successors for a given set of keys
     *
     * @param map
     * @param keys
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Set<V> safeGetAll(Map<K, ? extends Collection<V>> map, Collection<?> keys) {
        Set<V> result = new HashSet<V>();
        for(Object key : keys) {
            result.addAll(safeGet(map, key));
        }
        return result;
    }

    /**
     * TODO Add to collection utils
     *
     * @param map
     * @param key
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Collection<V> safeGetC(Map<K, ? extends Collection<V>> map, Object key)
    {
        Collection<V> values = map.get(key);
        return (values == null)
                ? Collections.<V>emptySet()
                : values;
    }


    public static <T> Set<T> transitiveGet(Map<T, ? extends Collection<T>> map, Object key)
    {
        Set<T> result = new HashSet<T>(safeGetC(map, key));
        Set<T> open = new HashSet<T>(result);
        Set<T> next = new HashSet<T>();

        do {
            for(T a : open) {
                for(T b : safeGetC(map, a)) {
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
        Map<T, Set<T>> open = map;
        Map<T, Set<T>> next = new HashMap<T, Set<T>>();

        for(;;) {
            // Check if any edge following an open edge would create a new edge
            for(Map.Entry<T, Set<T>> edge : open.entrySet()) {
                T nodeA = edge.getKey();
                for(T nodeB : edge.getValue()) {
                    for(T nodeC : safeGet(map, nodeB)) {
                        if(!containsEntry(map, nodeA, nodeC)) {
                            put(next, nodeA, nodeC);
                        }
                    }
                }
            }

            // Exit condition
            if(next.isEmpty()) {
                return map;
            }

            // Preparation of next iteration
            putAll(map, next);

            if(open == map) {
                open = new HashMap<T, Set<T>>();
            } else {
                open.clear();
            }

            Map<T, Set<T>> tmp = next;
            next = open;
            open = tmp;
        }
    }


	/**
	 * Find the nearest set of nodes that are parents of both given nodes.
	 *
	 *
	 * @param map A mapping from children to parents
	 * @param a
	 * @param b
	 */
	public static <T> Set<T> getCommonParent(Map<T, ? extends Collection<T>> map, T a, T b)
	{
		// The set of nodes for which all successors have already been visited
		Set<T> stableA = new HashSet<T>();
		Set<T> stableB = new HashSet<T>();

		// The set of nodes for which their successors have not been visited yet
		Set<T> frontierA = new HashSet<T>();
		Set<T> frontierB = new HashSet<T>();

		frontierA.add(a);
		frontierB.add(b);

		while(!frontierA.isEmpty() || !frontierB.isEmpty()) {

            Set<T> allA = Sets.union(frontierA, stableA);
            Set<T> allB = Sets.union(frontierB, stableB);

			Set<T> intersection = Sets.intersection(allA, allB);
			// If there is an overlap in the nodes, we are done
			if(!intersection.isEmpty()) {
				return intersection;
			}

			// No overlap, keep looking - Move up one level
			Set<T> nextFrontierA = MultiMaps.safeGetAll(map, frontierA);
			Set<T> nextFrontierB = MultiMaps.safeGetAll(map, frontierB);

			stableA.addAll(frontierA);
			stableB.addAll(frontierB);

			// OPTIMIZE Maybe this is more efficient: nextFrontierA.removeAll(Sets.intersection(frontierA,stableA));
			// The intersection with the smaller set first would avoid scannig all nodes
			// Note sure if java's removeAll already does such optimization
			nextFrontierA.removeAll(stableA);
			nextFrontierB.removeAll(stableB);

			frontierA = nextFrontierA;
			frontierB = nextFrontierB;
		}

		return Collections.emptySet();
	}

    /*
    public static <T> Map<T, Set<T>> transitiveClosureInPlace(Map<T, Set<T>> map)
    {
        return transitiveClosureInPlace(map, reverse(map));
    }

    public static <T> Map<T, Set<T>> transitiveClosureInPlace(Map<T, Set<T>> map, Map<T, Set<T>> rev)
    {
        Map<T, Set<T>> open = map;
        Map<T, Set<T>> next = new HashMap<T, Set<T>>();

        for(;;) {
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

            // Exit condition
            if(next.isEmpty()) {
                break;
            }

            // Preparation of next iteration
            putAll(map, next);

            if(open == map) {
                open = new HashMap<T, Set<T>>();
            } else {
                open.clear();
            }

            Map<T, Set<T>> tmp = next;
            next = open;
            open = tmp;

        }// while (!open.isEmpty());

        return map;
    }
    */

}