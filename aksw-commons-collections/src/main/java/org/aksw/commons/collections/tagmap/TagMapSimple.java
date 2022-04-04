package org.aksw.commons.collections.tagmap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ForwardingMap;

/**
 * Basic implementation of the SetIndex interface which simply sequentially scans a backing map.
 * Main use cases is performance comparison and convenience wrapping of existing maps.
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class TagMapSimple<K, V>
    extends ForwardingMap<K, Set<V>>
    implements TagMap<K, V>
{
    protected Map<K, Set<V>> map;

    public TagMapSimple() {
        this(new HashMap<>());
    }

    public TagMapSimple(Map<K, Set<V>> map) {
        super();
        this.map = map;
    }

    @Override
    protected Map<K, Set<V>> delegate() {
        return map;
    }

    @Override
    public TagMap<K, V> getAllSubsetsOf(Collection<?> set, boolean strict) {
        Map<K, Set<V>> resultMap = delegate().entrySet().stream()
            .filter(e -> set.containsAll(e.getValue()) && !(strict && set.equals(e.getValue())))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        TagMap<K, V> result = new TagMapSimple<>(resultMap);
        return result;
    }

    @Override
    public TagMap<K, V> getAllSupersetsOf(Collection<?> set, boolean strict) {
        Map<K, Set<V>> resultMap = delegate().entrySet().stream()
                .filter(e -> e.getValue().containsAll(set)  && !(strict && set.equals(e.getValue())))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        TagMap<K, V> result = new TagMapSimple<>(resultMap);
        return result;
    }

    @Override
    public TagMap<K, V> getAllEquisetsOf(Collection<?> set) {
        Map<K, Set<V>> resultMap = delegate().entrySet().stream()
            .filter(e -> set.equals(e.getValue()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        TagMap<K, V> result = new TagMapSimple<>(resultMap);
        return result;
    }
}
