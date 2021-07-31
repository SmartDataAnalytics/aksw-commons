package org.aksw.commons.rx.range;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

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
    public void put(Iterable<String> keySegments, Object obj) throws IOException {
        Iterable<String> effectiveKey = keyTransformer.apply(keySegments);
        super.put(effectiveKey, obj);
    }
}
