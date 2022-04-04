package org.aksw.commons.collections.tagmap;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


public class TagSetImpl<T>
    extends AbstractSet<T>
    implements TagSet<T>
{
    protected TagMap<T, Object> tagMap;
    protected Function<? super T, ? extends Collection<?>> tagFn;

    public TagSetImpl(TagMap<T, Object> tagMap, Function<? super T, ? extends Collection<?>> tagFn) {
        super();
        this.tagMap = tagMap;
        this.tagFn = tagFn;
    }

    protected Set<Object> deriveTags(T item) {
        Collection<?> features = tagFn.apply(item);
        Set<Object> result = features.stream().collect(Collectors.toSet());

        return result;
    }

    @Override
    public boolean add(T item) {
        Set<Object> tags = deriveTags(item);

        Object tmp = tagMap.put(item, tags);
        boolean result = tmp != null;
        return result;
    }


    @Override
    public boolean remove(Object o) {
        // TODO Auto-generated method stub
        return super.remove(o);
    }

    @Override
    public TagSet<T> getSuperItemsOf(T proto, boolean strict) {
        Set<Object> tags = deriveTags(proto);

        TagMap<T, Object> tmp = tagMap.getAllSupersetsOf(tags, strict);
        TagSet<T> result = new TagSetImpl<>(tmp, tagFn);
        return result;
    }

    @Override
    public TagSet<T> getSubItemsOf(T proto, boolean strict) {
        Set<Object> tags = deriveTags(proto);

        TagMap<T, Object> tmp = tagMap.getAllSubsetsOf(tags, strict);
        TagSet<T> result = new TagSetImpl<>(tmp, tagFn);
        return result;
    }

    @Override
    public Iterator<T> iterator() {
        return tagMap.keySet().iterator();
    }

    @Override
    public int size() {
        return tagMap.size();
    }
}