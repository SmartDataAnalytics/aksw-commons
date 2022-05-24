package org.aksw.commons.rx.lookup;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.rx.op.FlowableOperatorConditionalConcat;

import com.google.common.collect.Sets;

import io.reactivex.rxjava3.core.Flowable;

public class LookupServiceAddAbsentKeys<K, V>
    implements LookupService<K, V>
{
    protected LookupService<K, V> delegate;
    protected Function<? super K, ? extends V> defaultValueGenerator;

    public LookupServiceAddAbsentKeys(LookupService<K, V> delegate, Function<? super K, ? extends V> defaultValueGenerator) {
        this.delegate = delegate;
        this.defaultValueGenerator = defaultValueGenerator == null
                ? (K key) -> null
                : defaultValueGenerator;
    }

    @Override
    public Flowable<Entry<K, V>> apply(Iterable<K> t) {
        Set<K> requestedKeys = SetUtils.asSet(t);

        return delegate.apply(t)
            .lift(FlowableOperatorConditionalConcat.create(
                    AggBuilder.inputTransform(Entry::getKey, AggBuilder.hashSetSupplier()),
                    seenItems -> Flowable.fromIterable(
                            Sets.difference(requestedKeys, seenItems))
                        .map((K k) -> (Entry<K, V>)new SimpleEntry<K, V>(k, defaultValueGenerator.apply(k)))
            ));
    }
}
