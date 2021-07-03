package org.aksw.commons.rx.cache.range;

import java.nio.file.Path;
import java.util.function.Function;

public class KeyValueStoreFolder<K, V>
    implements KeyValueStore<K, V>
{

    protected Path folder;

    protected Function<K, String[]> keyToSegments;
    protected Function<Path, V> loader;



    @Override
    public V load(K key) {
        String[] segments = keyToSegments.apply(key);
        Path file = null; //PathUtils.resolve(folder, segments);


        // TODO create a lock
//        if (file.exists()) {
        V result = loader.apply(file);
//        }

        return result;
    }

    @Override
    public void save(V item) {
        // TODO Auto-generated method stub

    }
}
