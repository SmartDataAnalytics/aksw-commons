package org.aksw.commons.collections.tagmap;

import java.util.AbstractCollection;
import java.util.AbstractMap.SimpleEntry;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Tag map where the tag set acts as the key
 *
 * TODO For now do not modify the TagIndex objects returned as lookup results.
 *
 * @author raven
 *
 * @param <T>
 * @param <V>
 */
public class TagIndexImpl<T, V>
    extends AbstractCollection<Entry<Set<T>, Set<V>>>
    implements TagIndex<T, V>
{
    protected TagMap<Long, T> idToTags;
    protected SetMultimap<Long, V> idToValues;

    protected long nextId = 0;

    public TagIndexImpl(TagMap<Long, T> idToTags, SetMultimap<Long, V> idToValues) {
        super();
        this.idToTags = idToTags;
        this.idToValues = idToValues;
    }

    public void put(Set<T> tags, V value) {
        // Check if there is a matching entry in idToTags
        TagMap<Long, T> equiv = idToTags.getAllEquisetsOf(tags);//getAllSubsetsOf(tags, strict)
        Set<Long> existingKeys = equiv.keySet();
        long id;
        if(existingKeys.isEmpty()) {
            id = nextId++;
            idToTags.put(id, tags);
        } else {
            id = existingKeys.iterator().next();
        }

        idToValues.put(id, value);
    }

    @Override
    public int size() {
        int result = idToValues.size();
        return result;
    }

    @Override
    public Iterator<Entry<Set<T>, Set<V>>> iterator() {
        Iterator<Entry<Set<T>, Set<V>>> result = idToTags.entrySet().stream()
                .map(e -> (Entry<Set<T>, Set<V>>)new SimpleEntry<>(e.getValue(), idToValues.get(e.getKey())))
                .iterator();

        return result;
    }

    @Override
    public void removeAll(Set<T> tags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Set<T> tags, V value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public TagIndex<T, V> getAllSubsetsOf(Set<T> tags, boolean strict) {
        TagMap<Long, T> matches = idToTags.getAllSubsetsOf(tags, strict);
        TagIndex<T, V> result = new TagIndexImpl<>(matches, idToValues);

        return result;
    }

    @Override
    public TagIndex<T, V> getAllSupersetsOf(Set<T> tags, boolean strict) {
        TagMap<Long, T> matches = idToTags.getAllSupersetsOf(tags, strict);
        TagIndex<T, V> result = new TagIndexImpl<>(matches, idToValues);

        return result;
    }


    @Override
    public TagIndex<T, V> getAllEquisetsOf(Set<T> tags) {
        TagMap<Long, T> matches = idToTags.getAllEquisetsOf(tags);
        TagIndex<T, V> result = new TagIndexImpl<>(matches, idToValues);

        return result;
    }

    public static <T, V> TagIndex<T, V> create() {
        TagIndex<T, V> result = new TagIndexImpl<>(new TagMapSetTrie<>(), HashMultimap.create());
        return result;
    }

    public static <T, V> TagIndex<T, V> create(Comparator<? super T> comparator) {
        TagIndex<T, V> result = new TagIndexImpl<>(new TagMapSetTrie<>(comparator), HashMultimap.create());
        return result;
    }

    // TODO Provide an method to iterate over the content...

}
