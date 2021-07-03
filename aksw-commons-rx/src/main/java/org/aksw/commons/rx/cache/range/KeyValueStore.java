package org.aksw.commons.rx.cache.range;

public interface KeyValueStore<K, V> {
    V load(K key);
    // void save(K key, V value);
    void save(V item); // Is V assumed to hold a reference to the key? likely yes.
}
