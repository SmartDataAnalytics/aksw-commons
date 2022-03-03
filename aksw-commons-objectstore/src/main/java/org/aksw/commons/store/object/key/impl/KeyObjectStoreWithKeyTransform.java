package org.aksw.commons.store.object.key.impl;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.aksw.commons.store.object.key.api.KeyObjectStore;

import com.google.common.collect.Iterables;

public class KeyObjectStoreWithKeyTransform
    extends KeyObjectStoreDelegateBase
{
    // protected List<String> prefix;
    protected Function<? super Iterable<String>, ? extends Iterable<String>> keyTransformer;

    public KeyObjectStoreWithKeyTransform(KeyObjectStore delegate, Function<? super Iterable<String>, ? extends Iterable<String>> keyTransformer) {
        super(delegate);
        this.keyTransformer = keyTransformer;
    }

    public static KeyObjectStore wrapWithPrefix(KeyObjectStore delegate, List<String> prefix) {
        return new KeyObjectStoreWithKeyTransform(delegate, keySegments -> Iterables.concat(prefix, keySegments));
    }


    public static KeyObjectStore wrap(KeyObjectStore delegate, Function<? super Iterable<String>, ? extends Iterable<String>> keyTransformer) {
        return new KeyObjectStoreWithKeyTransform(delegate, keyTransformer);
    }


    @Override
    public <T> T get(Iterable<String> keySegments) throws IOException, ClassNotFoundException {
        Iterable<String> effectiveKey = keyTransformer.apply(keySegments);
        return super.get(effectiveKey);
    }

    @Override
    public <T> T computeIfAbsent(Iterable<String> keySegments, Callable<T> initializer)
            throws IOException, ClassNotFoundException, ExecutionException {
        Iterable<String> effectiveKey = keyTransformer.apply(keySegments);
        return super.computeIfAbsent(effectiveKey, initializer);
    }

    @Override
    public void put(Iterable<String> keySegments, Object obj) throws IOException {
        Iterable<String> effectiveKey = keyTransformer.apply(keySegments);
        super.put(effectiveKey, obj);
    }
}
