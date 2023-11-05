package org.aksw.commons.util.cache;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Converter;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class TestCacheView {
    @Test
    public void testCacheView() {
        Cache<Entry<String, Integer>, String> sharedCache = CacheBuilder.newBuilder().build();

        Converter<Integer, Entry<String, Integer>> convA = Converter.from(i -> Map.entry("a", i), Entry::getValue);
        Converter<Integer, Entry<String, Integer>> convB = Converter.from(i -> Map.entry("b", i), Entry::getValue);

        Cache<Integer, String> cacheA = new CacheView<>(sharedCache, convA, e -> "a".equals(e.getKey()));
        Cache<Integer, String> cacheB = new CacheView<>(sharedCache, convB, e -> "b".equals(e.getKey()));

        for (int i = 0; i < 3; ++i) {
            int v = i;
            CacheUtils.get(cacheA, v, () -> "a" + v);
        }

        for (int j = 0; j < 3; ++j) {
            int v = j;
            CacheUtils.get(cacheB, v, () -> "b" + v);
        }

        Assert.assertEquals("a1", cacheA.getIfPresent(1));
        Assert.assertEquals("b2", cacheB.getIfPresent(2));

        // System.out.println(sharedCache.asMap());
        // System.out.println(CacheUtils.getIfPresent(cacheA, 2));
        // System.out.println(CacheUtils.getIfPresent(cacheB, 2));
    }
}
