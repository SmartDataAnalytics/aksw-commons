package org.aksw.commons.collections.cache;

import java.util.Iterator;

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
    protected Cache<T> cache;
    protected Iterator<T> delegate;
    protected int offset;

    public CachingIterator(Cache<T> cache, Iterator<T> delegate) {
        this(cache, delegate, 0);
    }

    public CachingIterator(Cache<T> cache, Iterator<T> delegate, int offset) {
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
        int cacheSize = cache.getCurrentSize();
        if(offset < cacheSize) { // logical or: assuming offset == cache.size()
            result = true;
        } else if(cache.isComplete() || cache.isAbandoned()) {
            result = false;
        } else {
            result = delegate.hasNext();

            if(!result) {
                cache.setComplete();//true);
            }
        }

        return result;
    }

    @Override
    public T next() {
        T result;

        //List<T> cacheData = cache.getData();

        // Inform all possibly waiting client on the cache
        // that data has been added so that they can commence
        //synchronized(cache) {

            // Check if item at index i is already cached
        if(offset < cache.getCurrentSize()) {
            result = cache.get(offset);
        } else {
            result = delegate.next();

            // It is important to use cache.add here (instead of cacheDada.add)
            // because the former is expected to call cache.notifyAll()
            cache.add(result);
            //cache.add(result);
            //cache.notifyAll();
        }
        //}

        ++offset;
        return result;
    }
}