package org.aksw.commons.store.object.key.impl;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.aksw.commons.store.object.key.api.KeyObjectStore;

public interface KeyObjectStoreDelegate
    extends KeyObjectStore
{
    KeyObjectStore getDelegate();

    @Override
    default void put(Iterable<String> keySegments, Object obj) throws IOException {
        getDelegate().put(keySegments, obj);
    }

    @Override
    default <T> T get(Iterable<String> keySegments) throws IOException, ClassNotFoundException {
        return getDelegate().get(keySegments);
    }

    @Override
    default <T> T computeIfAbsent(Iterable<String> keySegments, Callable<T> initializer)
            throws IOException, ClassNotFoundException, ExecutionException {
        return getDelegate().computeIfAbsent(keySegments, initializer);
    }
}

