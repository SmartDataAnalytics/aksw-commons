package org.aksw.commons.collections.multimaps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.collections.MultiMaps;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

/**
 * Created by IntelliJ IDEA.
 * User: raven
 * Date: 4/25/11
 * Time: 9:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class MultimapUtils {

    public static <G, T> Multimap<G, T> groupBy(Iterable<T> items, Function<T, G> itemToGroupKey, Multimap<G, T> result) {
        for(T item : items) {
            G groupKey = itemToGroupKey.apply(item);
            result.put(groupKey, item);
        }

        return result;
    }

    public static <K, V> SetMultimap<K, V> newSetMultimap(boolean identityKeys, boolean identityValues) {
        Map<K, Collection<V>> keys = identityKeys ? Maps.newIdentityHashMap() : new HashMap<>();
        com.google.common.base.Supplier<Set<V>> values = identityValues ? Sets::newIdentityHashSet : HashSet::new;

        return Multimaps.newSetMultimap(keys, values);
    }

    public static <K, V> SetMultimap<K, V> newIdentitySetMultimap() {
        return Multimaps.newSetMultimap(Maps.newIdentityHashMap(), Sets::newIdentityHashSet);
    }

    public static <K, V> ListMultimap<K, V> newIdentityListMultimap() {
        return Multimaps.newListMultimap(Maps.newIdentityHashMap(), ArrayList::new);
    }

    public static <K, V> Set<V> getAll(Multimap<K, V> multiMap, Collection<K> keys) {
        Set<V> result = keys.stream()
                .flatMap(k -> multiMap.get(k).stream())
                .collect(Collectors.toSet());
        return result;
    }


    /**
     * A transitive get in both directions
     *
     * @param map
     */
    public static <T> Set<T> transitiveGetBoth(IBiSetMultimap<T, T> map, Object key)
    {
        Set<T> result = MultiMaps.transitiveGet(map.asMap(), key);
        result.addAll(MultiMaps.transitiveGet(map.getInverse().asMap(), key));

        return result;
    }

    /**
     * Helper function to convert a multimap into a map.
     * Each key may only have at most one corresponding value,
     * otherwise an exception will be thrown.
     *
     * @param mm
     * @return
     */
    public static <K, V> Map<K, V> toMap(Map<K, ? extends Collection<V>> mm) {
        // Convert the multimap to an ordinate map
        Map<K, V> result = new HashMap<K, V>();
        for(Entry<K, ? extends Collection<V>> entry : mm.entrySet()) {
            K k = entry.getKey();
            Collection<V> vs = entry.getValue();

            if(!vs.isEmpty()) {
                if(vs.size() > 1) {
                    throw new RuntimeException("Ambigous mapping for " + k + ": " + vs);
                }

                V v = vs.iterator().next();
                result.put(k, v);
            }
        }

        return result;
    }
}
