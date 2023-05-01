package org.aksw.commons.rx.lookup;

import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;

import io.reactivex.rxjava3.core.Flowable;

public class LookupServiceFilterKey<K, V>
    implements LookupService<K, V>
{
    protected LookupService<K, V> delegate;
    protected Predicate<? super K> filter;

    public LookupServiceFilterKey(LookupService<K, V> delegate, Predicate<? super K> filter) {
        this.delegate = delegate;
        this.filter = filter;
    }

    public static <K, V> LookupService<K, V> create(LookupService<K, V> delegate, Predicate<? super K> filter) {
        return new LookupServiceFilterKey<K, V>(delegate, filter);
    }

    @Override
    public Flowable<Entry<K, V>> apply(Iterable<K> t) {
        List<K> keys = Streams.stream(t)
                .filter(key -> filter.test(key))
                .collect(Collectors.toList());

        return delegate.apply(keys);
    }
}
