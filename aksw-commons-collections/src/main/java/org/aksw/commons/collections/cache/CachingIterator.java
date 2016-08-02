package org.aksw.commons.collections.cache;

import java.util.Iterator;
import java.util.List;

/**
 * In iterator that adds items to a cache as it proceeds.
 * It is valid for other iterators with the same delegate and cache to exist.
 * However, no iterator must be positioned beyond the cache.data's size.
 *
 *
 * @author raven
 *
 * @param <T>
 * @param <C>
 */
public class CachingIterator<T>
    implements Iterator<T>
{
    protected Cache<? extends List<T>> cache;
    protected Iterator<T> delegate;
    protected int offset;

    public CachingIterator(Cache<? extends List<T>> cache, Iterator<T> delegate) {
        this(cache, delegate, 0);
    }

    public CachingIterator(Cache<? extends List<T>> cache, Iterator<T> delegate, int offset) {
        super();
        this.cache = cache;
        this.delegate = delegate;
        this.offset = offset;
    }

    /**
     * The cache's isComplete flag is only set if a call to hasNext returns false.
     */
    @Override
    public boolean hasNext() {
        boolean result;
        int cacheSize = cache.getData().size();
        if(offset < cacheSize) { // logical or: assuming offset == cache.size()
            result = true;
        } else if(cache.isComplete()) {
            result = false;
        } else {
            result = delegate.hasNext();

            if(!result) {
                cache.setComplete(true);
            }
        }

        return result;
    }

    @Override
    public T next() {
        T result;

        List<T> cacheData = cache.getData();

        // Check if item at index i is already cached
        if(offset < cacheData.size()) {
            result = cacheData.get(offset);
        } else {
            result = delegate.next();

            // Inform all possibly waiting client on the cache
            // that data has been added so that they can commence
            synchronized(cache) {
                cacheData.add(result);
                cache.notifyAll();
            }
        }

        ++offset;
        return result;
    }
}