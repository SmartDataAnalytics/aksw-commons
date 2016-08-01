package org.aksw.commons.collections.cache;

import java.util.List;

import com.google.common.collect.AbstractIterator;

/**
 * An iterator that reads data from a cache, and waits for more data to become available
 *
 * @author raven
 *
 * @param <T>
 */
public class BlockingCacheIterator<T>
    extends AbstractIterator<T>
{
    protected Cache<? extends List<? extends T>> cache;
    protected int offset;

    public BlockingCacheIterator(Cache<? extends List<? extends T>> cache) {
        this(cache, 0);
    }

    public BlockingCacheIterator(Cache<? extends List<? extends T>> cache, int offset) {
        super();
        this.cache = cache;
        this.offset = offset;
    }

    public int getOffset() {
        return offset;        
    }
    
    public Cache<? extends List<? extends T>> getCache() {
        return cache;
    }
    
    
    @Override
    public T computeNext() {
        List<? extends T> data = cache.getData();

        T result;
        for(;;) {
            if(offset < data.size()) {
                result = data.get(offset);
                ++offset;
                break;
            } else if(cache.isComplete() || cache.isAbanoned()) {
                result = endOfData();
                break;
                //throw new IndexOutOfBoundsException();
//            } else if(cache.isAbanoned()) {
                //throw new RuntimeException("Cache was abandoned");
                
            } else {
                try {
                    synchronized(cache) {
                        cache.wait();
                    }
                } catch (InterruptedException e) {
                }
            }
        }

        return result;
    }
}
