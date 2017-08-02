package org.aksw.commons.collections.cache;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.collect.Iterables;

/**
 * An iterable over an iterator that caches the iterator's results.
 *
 * @author raven
 *
 * @param <T>
 */
public class CachingIterable<T>
    implements Iterable<T>
{
    protected Iterator<T> delegate;
    protected Cache<T> cache; // = new Cache<T, C>();

    public CachingIterable(Iterator<T> delegate) {
        super();
        this.delegate = delegate;
        this.cache = new CacheImpl<>(new ArrayList<>());
    }

    public CachingIterable(Iterator<T> delegate, Cache<T> cache) {
        super();
        this.delegate = delegate;
        this.cache = cache;
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> result = new CachingIterator<T>(cache, delegate, 0);
        return result;
    }

    @Override
    public String toString() {
        String result = Iterables.toString(this);
        return result;
    }

//    public static <T> Iterable<T> newArrayListCachingIterable(Iterator<T> delegate) {
//        Cache<T> cache = new Cache<T, List<T>>(new ArrayList<T>());
//        Iterable<T> result = new CachingIterable<T, List<T>>(delegate, cache, 0);
//        return result;
//    }

}