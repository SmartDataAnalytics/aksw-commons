package org.aksw.commons.store.object.key.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.aksw.commons.store.object.key.api.KeyObjectStore;

import com.google.common.collect.Lists;

public class KeyObjectStoreFromMap
    implements KeyObjectStore
{
    protected Map<List<String>, Object> keyToObject;

    public KeyObjectStoreFromMap() {
        this(new ConcurrentHashMap<>());
    }

    public KeyObjectStoreFromMap(Map<List<String>, Object> keyToObject) {
        super();
        this.keyToObject = keyToObject;
    }

    public Map<List<String>, Object> getMap() {
        return keyToObject;
    }

    @Override
    public void put(Iterable<String> keySegments, Object obj) throws IOException {
        keyToObject.put(Lists.newArrayList(keySegments), obj);
    }

    @Override
    public <T> T get(Iterable<String> keySegments) throws IOException, ClassNotFoundException {
        Object obj = keyToObject.get(Lists.newArrayList(keySegments));
        return (T)obj;
    }

    @Override
    public <T> T computeIfAbsent(Iterable<String> keySegments, Callable<T> initializer)
            throws IOException, ClassNotFoundException, ExecutionException {
        Object obj = keyToObject.computeIfAbsent(Lists.newArrayList(keySegments), key -> {
                try {
                    return initializer.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

        return (T)obj;
    }

}
