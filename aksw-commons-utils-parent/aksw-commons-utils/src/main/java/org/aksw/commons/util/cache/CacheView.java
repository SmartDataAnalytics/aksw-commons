package org.aksw.commons.util.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.base.Converter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Streams;

/**
 * A view over a cache which can place 'local' keys into a shared cache.
 * For example, local keys could be offsets in a file, whereas global keys
 * include the file name.
 *
 * @param <KF> The frontend key type
 * @param <KB> The backend key type
 * @param <V> The value type
 */
public class CacheView<KF, KB, V>
    implements Cache<KF, V>
{
    /** The delegate cache*/
    protected Cache<KB, V> delegate;

    protected Converter<KF, KB> keyMapper;

    /**
     * A predicate that hides cache entries unrelated to this cache view.
     * Note that every backend key derived from this view must succeed the filter.
     * This means:
     * backendFilter.test(keyMapper.convert(frontendKey)) must always evaluate to true.
     */
    protected Predicate<KB> backendFilter;

    public CacheView(Cache<KB, V> delegate, Converter<KF, KB> keyMapper, Predicate<KB> backendFilter) {
        super();
        this.delegate = delegate;
        this.keyMapper = keyMapper;
        this.backendFilter = backendFilter;
    }

    public Cache<KB, V> getDelegate() {
        return delegate;
    }

    protected KB safeToBackend(KF key) {
        KB result = keyMapper.convert(key);
        if (!backendFilter.test(result)) {
            throw new IllegalStateException("Converted key was rejected by filter. Input: " + key + " Output: " + result);
        }
        return result;
    }

    protected List<KB> convert(Object key) {
        KF in;
        try {
            @SuppressWarnings("unchecked")
            KF tmp = (KF)key;
            in = tmp;
        } catch (ClassCastException e) {
            return Collections.emptyList();
        }
        KB tmp = safeToBackend(in);
        return Collections.singletonList(tmp);
    }

    @Override
    public V getIfPresent(Object key) {
        Cache<KB, V> backend = getDelegate();
        V result = null;
        for (KB backendKey : convert(key)) {
            result = backend.getIfPresent(backendKey);
        }
        return result;
    }

    @Override
    public V get(KF key, Callable<? extends V> loader) throws ExecutionException {
        Cache<KB, V> backend = getDelegate();
        KB backendKey = keyMapper.convert(key);
        V result = backend.get(backendKey, loader);
        return result;
    }

    @Override
    public ImmutableMap<KF, V> getAllPresent(Iterable<? extends Object> keys) {
        Cache<KB, V> backend = getDelegate();

        @SuppressWarnings("unchecked")
        List<KB> backendKeys = Streams.stream(keys)
                .map(k -> (KF)k)
                .map(keyMapper::convert)
                .collect(Collectors.toList());

        Builder<KF, V> mapBuilder = ImmutableMap.builder();

        ImmutableMap<KB, V> tmp = backend.getAllPresent(backendKeys);
        tmp.forEach((backendKey, value) -> {
            KF frontendKey = keyMapper.reverse().convert(backendKey);
            mapBuilder.put(frontendKey, value);
        });
        ImmutableMap<KF, V> result = mapBuilder.build();
        return result;
    }
    @Override
    public void put(KF key, V value) {
        Cache<KB, V> backend = getDelegate();
        KB backendKey = keyMapper.convert(key);
        backend.put(backendKey, value);
    }

    @Override
    public void putAll(Map<? extends KF, ? extends V> m) {
        Cache<KB, V> backend = getDelegate();
        Map<KB, V> map = m.entrySet().stream().collect(Collectors.toMap(e -> safeToBackend(e.getKey()), Entry::getValue));
        backend.putAll(map);
    }

    @Override
    public void invalidate(Object key) {
        Cache<KB, V> backend = getDelegate();
        for(KB backendKey : convert(key)) {
            backend.invalidate(backendKey);
        }
    }

    @Override
    public void invalidateAll(Iterable<? extends Object> keys) {
        Cache<KB, V> backend = getDelegate();
        List<KB> backendKeys = Streams.stream(keys).flatMap(key -> convert(key).stream()).collect(Collectors.toList());
        backend.invalidateAll(backendKeys);
    }

    @Override
    public void invalidateAll() {
        // Only invalidate the keys that match the filter
        Cache<KB, V> backend = getDelegate();
        Collection<KB> backendKeys = backend.asMap().keySet().stream().filter(backendFilter).collect(Collectors.toList());
        backend.invalidateAll(backendKeys);
    }

    @Override
    public long size() {
        Cache<KB, V> backend = getDelegate();
        long result = backend.asMap().keySet().stream().filter(backendFilter).count();
        return result;
    }

    @Override
    public CacheStats stats() {
        Cache<KB, V> backend = getDelegate();
        CacheStats result = backend.stats();
        return result;
    }
    @Override
    public ConcurrentMap<KF, V> asMap() {
        // TODO We could adapt our MapFromKeyConverter class but this one is in the collections module
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanUp() {
        Cache<KB, V> backend = getDelegate();
        backend.cleanUp();
    }
}
