package org.aksw.commons.collections.cache;

import java.util.List;
import java.util.concurrent.BlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(BlockingCacheIterator.class);

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

        	// TODO Get rid of needless synchronization for each item
            synchronized(cache) {
                if(offset < data.size()) {
                    result = data.get(offset);
                    ++offset;
                    break;
                } else {
    //            } else if(cache.isComplete() || cache.isAbanoned()) {
    //                result = endOfData();
    //                break;
    //            } else {
                    //throw new IndexOutOfBoundsException();
    //            } else if(cache.isAbanoned()) {
                    //throw new RuntimeException("Cache was abandoned");
                            // Re-check whether the cache has been completed
                    if(cache.isComplete() || cache.isAbanoned()) {
                        result = endOfData();
                        break;
                    } else {
                        try {
                            cache.wait();
                        } catch (InterruptedException e) {
                            logger.warn("Exception", e);
                        }
                    }
                }
            }
        }

        return result;
    }
}
