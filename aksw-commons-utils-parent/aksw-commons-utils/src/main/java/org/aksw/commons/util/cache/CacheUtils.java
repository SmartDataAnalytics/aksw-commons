package org.aksw.commons.util.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;

/** Utils for guava caches. Methods do not declare exceptions and the cache may be null. */
public class CacheUtils {
    public static void invalidateAll(Cache<?, ?> cache) {
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    public static <K, V> V getIfPresent(Cache<K, V> cache, K key) {
        V result = cache != null ? cache.getIfPresent(key) : null;
        return result;
    }

    public static <K, V> V get(Cache<K, V> cache, K key, Callable<? extends V> callable) {
        V result;
        if (cache == null) {
            try {
                result = callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                result = cache.get(key, callable);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
}
