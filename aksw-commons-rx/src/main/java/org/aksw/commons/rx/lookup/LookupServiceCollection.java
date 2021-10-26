package org.aksw.commons.rx.lookup;

import java.util.Collection;

import com.google.common.base.Function;

public interface LookupServiceCollection<K, V>
    extends Function<Iterable<K>, Collection<V>>
{
//    Map<K, V> lookup(Iterable<K> keys);
}
