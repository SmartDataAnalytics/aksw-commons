package org.aksw.commons.collections.tagmap;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

public class TagMapSetTrie<K, V>
    extends AbstractMap<K, Set<V>>
    implements TagMap<K, V>
{
    protected SetTrie<K, V> setTrie;

    public TagMapSetTrie() {
        this(null);
    }

    public TagMapSetTrie(Comparator<? super V> comparator) {
        super();
        setTrie = new SetTrie<K, V>(comparator);
    }

    @Override
    public Set<V> put(K key, Set<V> set) {
        Set<V> result = setTrie.put(key, set);
        return result;
    }

    @Override
    public Set<V> remove(Object key) {
        Set<V> result = setTrie.remove(key);
        return result;
    }

    @Override
    public Set<Entry<K, Set<V>>> entrySet() {
        Map<K, Set<V>> tmp = Collections.unmodifiableMap(setTrie.getAllSupersetsOf(Collections.<V>emptySet()));
        return tmp.entrySet();
    }

    @Override
    public void clear() {
        setTrie.clear();
    }

    @Override
    public TagMap<K, V> getAllSubsetsOf(Collection<?> set, boolean strict) {
        Map<K, Set<V>> resultMap = setTrie.getAllSubsetsOf(set);
        TagMap<K, V> result = new TagMapSimple<>(resultMap);
        return result;
    }


    @Override
    public TagMap<K, V> getAllSupersetsOf(Collection<?> set, boolean strict) {
        Map<K, Set<V>> resultMap = setTrie.getAllSupersetsOf(set);
        TagMap<K, V> result = new TagMapSimple<>(resultMap);
        return result;
    }

    @Override
    public TagMap<K, V> getAllEquisetsOf(Collection<?> set) {
        Map<K, Set<V>> resultMap = setTrie.getAllEquisetsOf(set);
        TagMap<K, V> result = new TagMapSimple<>(resultMap);
        return result;
    }

}
