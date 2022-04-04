package org.aksw.commons.collections.tagmap;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ForwardingMap;

public abstract class TagMapForwarding<K, V>
    extends ForwardingMap<K, Set<V>>
    implements TagMap<K, V>
{
    @Override
    protected abstract TagMap<K, V> delegate();

    @Override
    public TagMap<K, V> getAllSubsetsOf(Collection<?> set, boolean strict) {
        TagMap<K, V> result = delegate().getAllSubsetsOf(set, strict);
        return result;
    }

    @Override
    public TagMap<K, V> getAllSupersetsOf(Collection<?> set, boolean strict) {
        TagMap<K, V> result = delegate().getAllSupersetsOf(set, strict);
        return result;
    }
}
