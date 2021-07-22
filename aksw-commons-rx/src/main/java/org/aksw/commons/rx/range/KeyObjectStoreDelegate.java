package org.aksw.commons.rx.range;

import java.io.IOException;

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
}
