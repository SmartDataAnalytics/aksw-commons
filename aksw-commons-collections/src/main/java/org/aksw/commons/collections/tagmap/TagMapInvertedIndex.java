package org.aksw.commons.collections.tagmap;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class TagMapInvertedIndex<V, K>
    extends AbstractMap<K, Set<V>>
    implements TagMap<K, V>
{

    protected Map<V, Integer> tagToCount;
    protected Multimap<V, Set<V>> tagToTagSets;


    // Maybe the following two maps could be replaced with BiHashMultimap<K, V>

    // FIXME: We could use our ReversibleMap implementation to abstract the next two fields
    protected Multimap<Set<V>, K> tagSetToKeys;
    protected Map<K, Set<V>> keyToTagSet;



//    public ContainmentMapImpl(Map<K, Integer> tagToCount,
//            Multimap<K, Set<K>> tagToTagSets,
//            Multimap<Set<K>, V> tagSetToValues,
//            Multimap<V, Set<K>> valueToTagSets) {
//        super();
//        this.tagToCount = tagToCount;
//        this.tagToTagSets = tagToTagSets;
//        this.tagSetToValues = tagSetToValues;
//        this.valueToTagSets = valueToTagSets;
//    }
    public TagMapInvertedIndex() {
        super();
        this.tagToCount = new HashMap<>();
        this.tagToTagSets = HashMultimap.create();
        this.tagSetToKeys = HashMultimap.create();
        this.keyToTagSet = new HashMap<>();
    }

    @Override
    public Set<K> keySet() {
        Set<K> result = keyToTagSet.keySet();
        return result;
    }

    @Override
    public Collection<Set<V>> values() {
        Collection<Set<V>> result = tagSetToKeys.keySet();
        return result;
    }

    @Override
    public Set<Entry<K, Set<V>>> entrySet() {
        Set<Entry<K, Set<V>>> result = keyToTagSet.entrySet();
        return result;
    };

//    public Set<V> put(K key, Collection<?> tagSet) {
//
//    }

    @Override
    public Set<V> put(K key, Set<V> tagSet) {
        // Remove possibly prior association of the key
        remove(key);

        //tagSetToValues.asMap().
        tagSetToKeys.put(tagSet, key);
        tagSet.forEach(tag -> {
            tagToTagSets.put(tag, tagSet);
            tagToCount.merge(tag, 1, Integer::sum);
        });

        keyToTagSet.put(key, tagSet);

        return tagSet;
    }

    @Override
    public Set<V> remove(Object key) {
        @SuppressWarnings("unchecked")
        Set<V> result = keyToTagSet.get(key);

        // Decrement reference count of each tag
        // TODO Remove if counter reaches zero
        result.forEach(tag -> tagToCount.merge(tag, 1, (a, b) -> a - b));

        return result;
    }


    protected TagMap<K, V> getByLeastUsedTagAndPredicate(Collection<?> prototype, Predicate<Set<V>> tagSetPredicate) {
        //Set<Entry<Set<K>, Set<V>>> result;

        Object leastUsedTag = prototype
            .stream()
            .map(k -> new SimpleEntry<>(k, tagToCount.getOrDefault(k, 0)))
            .min((a, b) -> a.getValue() - b.getValue())
            .map(Entry::getKey)
            .orElse(null);

        //Stream<Set<K>> baseStream;
        Stream<Entry<K, Set<V>>> baseStream;
        if(leastUsedTag != null) {
            Collection<Set<V>> rawTagSets = tagToTagSets.asMap().get(leastUsedTag);
            baseStream = rawTagSets
                    .stream()
                    .filter(tagSet -> tagSetPredicate.test(tagSet)) //tagSet.containsAll(prototype))
                    .flatMap(tagSet -> {
                        Collection<K> v = tagSetToKeys.get(tagSet);

                        Stream<Entry<K, Set<V>>> r = v.stream()
                            .map(w -> new SimpleEntry<>(w, tagSet));

                        return r;
                    });

        } else {
            //baseStream = tagToTagSets.values().stream();
            baseStream = keyToTagSet.entrySet().stream();
                    //.map(v -> new SimpleEntry<>(Collections.<K>emptySet(), v));
            //baseStream = Stream.of(Collections.emptySet());
        }

//        Stream<Entry<Set<K>, V>> taggedStream = baseStream
//                .flatMap(tagSet -> {
//                    Collection<V> v = tagSetToValues.get(tagSet);
//
//                    Stream<Entry<Set<K>, V>> r = v.stream()
//                        .map(w -> new SimpleEntry<>(tagSet, w));
//
//                    return r;
//                });

        Map<K, Set<V>> resultMap = baseStream.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        TagMap<K, V> result = new TagMapSimple<>(resultMap);

        //Collection<Entry<Set<V>, K>> result = baseStream.collect(Collectors.toList());
        return result;
    }

    /**
     * Return every entry of this featureMap whose associated feature set
     * is a super set of the given one.
     *
     */
    @Override
    public TagMap<K, V> getAllSupersetsOf(Collection<?> prototype, boolean strict) {
        TagMap<K, V> result = getByLeastUsedTagAndPredicate(prototype, (tagSet) -> tagSet.containsAll(prototype));

        return result;
    }


    @Override
    public TagMap<K, V> getAllSubsetsOf(Collection<?> prototype, boolean strict) {
        // get the count if we used index lookup
        int indexCount = prototype.isEmpty() ? Integer.MAX_VALUE : prototype.stream().mapToInt(tag -> tagToCount.getOrDefault(tag, 0)).sum();
        int totalCount = keyToTagSet.size();

        Stream<Set<V>> tagSetStream;
//        float scanThreshold = 0.3f;
//        float val = scanThreshold * totalCount;
//        if(indexCount > val) {
        boolean useScan = indexCount >= totalCount;
        if(useScan) {
            // perform a scan
            tagSetStream = tagSetToKeys.keySet().stream();
        } else {
            tagSetStream = Stream.concat(
                    Stream.of(Collections.<V>emptySet()),
                    prototype.stream()
                        .flatMap(tag -> tagToTagSets.asMap().get(tag).stream())
                        .distinct());
        }

        Stream<Entry<K, Set<V>>> baseStream = tagSetStream
            .filter(tagSet -> prototype.containsAll(tagSet))
            .flatMap(tagSet -> {
                Collection<K> values = tagSetToKeys.get(tagSet);
                Stream<Entry<K, Set<V>>> r = values.stream()
                        .map(w -> new SimpleEntry<>(w, tagSet));
                return r;
            });

        Map<K, Set<V>> resultMap = baseStream.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        TagMap<K, V> result = new TagMapSimple<>(resultMap);

        return result;
    }


    @Override
    public TagMap<K, V> getAllEquisetsOf(Collection<?> prototype) {
        Set<Object> tmp = new HashSet<>(prototype);
        TagMap<K, V> result = getByLeastUsedTagAndPredicate(prototype, (tagSet) -> tagSet.size() == tmp.size() && tagSet.containsAll(tmp));

        return result;
    }

    @Override
    public String toString() {
        return "FeatureMapImpl [tagToCount=" + tagToCount
                + ", tagToTagSets=" + tagToTagSets + ", tagSetToValues="
                + tagSetToKeys + ", valueToTagSets=" + keyToTagSet + "]";
    }

//    @Override
//    public Iterator<Entry<K, Set<V>>> iterator() {
//        Iterator<Entry<K, Set<V>>> result = keyToTagSet.entrySet().iterator();
//        return result;
//    }

    @Override
    public int size() {
        int result = tagSetToKeys.size();
        return result;
    }

    @Override
    public Set<V> get(Object key) {
        Set<V> result = keyToTagSet.get(key);
        return result;
    }


//    @Override
//    public boolean removeValue(Object v) {
//        Collection<K> keys = tagSetToKeys.asMap().get(v);
//
//        boolean result = false;
//        for(K key : keys) {
//            Set<V> tmp = remove(key);
//
//            result = result || tmp != null;
//        }
//        return result;
//    }

}
