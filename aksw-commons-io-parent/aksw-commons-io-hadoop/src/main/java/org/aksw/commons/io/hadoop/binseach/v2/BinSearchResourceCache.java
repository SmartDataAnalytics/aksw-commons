package org.aksw.commons.io.hadoop.binseach.v2;

import java.util.function.Supplier;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class BinSearchResourceCache {
    record CacheEntry(BinSearchLevelCache levelCache, Cache<Long, Block> blockCache) {}

    protected Cache<Object, CacheEntry> resourceCache;

    /** Factory for the caches of individual resources. */
    protected Supplier<CacheEntry> cacheFactory;

    public BinSearchResourceCache(int maxCacheSize) {
        this(Caffeine.newBuilder().maximumSize(maxCacheSize).build(), () -> {
            return new CacheEntry(BinSearchLevelCache.dftCache(), Caffeine.newBuilder().maximumSize(16).build());
        });
    }

    public BinSearchResourceCache(Cache<Object, CacheEntry> resourceCache, Supplier<CacheEntry> cacheFactory) {
        this.resourceCache = resourceCache;
        this.cacheFactory = cacheFactory;
    }

    public CacheEntry getOrCreate(Object key) {
        return resourceCache.get(key, k -> cacheFactory.get());
    }
}
