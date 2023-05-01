package org.aksw.commons.cache.async;

import org.aksw.commons.util.ref.Ref;

public interface ClaimingMap<K, V> {
    Ref<V> claim(K key);
}
