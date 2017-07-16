package org.aksw.commons.collections.cache;

import java.util.Iterator;
import java.util.List;

import org.aksw.commons.collections.PrefetchIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An iterator that reads data from a cache, and waits for more data to become available
 *
 * @author raven
 *
 * @param <T>
 */
//public class BlockingCacheIterator<T>
//    extends PrefetchIterator<T>
//{
//    private static final Logger logger = LoggerFactory.getLogger(BlockingCacheIterator.class);
//
//    protected Cache<T> cache;
//    protected IndexBasedIterator<T> priorIt = null;
//
//    public BlockingCacheIterator(Cache<T> cache) {
//        this(cache, 0);
//    }
//
//    public BlockingCacheIterator(Cache<T> cache, int offset) {
//        super();
//        this.cache = cache;
//        //this.offset = offset;
//    }
//
////    public int getOffset() {
////        return offset;
////    }
//
//    public Cache<T> getCache() {
//        return cache;
//    }
//
//    @Override
//    protected Iterator<T> prefetch() throws Exception {
//        int offset = priorIt == null ? 0 : priorIt.getMaxIndex();
//
//        List<T> data = cache.getData();
//
//        // Wait until the cache is abandoned, or more data to becomes available or the cache is comp
//        int maxIndex;
//        synchronized(cache) {
//            while(offset == (maxIndex = data.size()) && !cache.isComplete() && !cache.isAbandoned()) {
//                cache.wait();
//            }
//        }
//
//        // Create an iterator up to the size of the data
//        priorIt = offset == maxIndex || cache.isAbandoned()
//                ? null
//                : new IndexBasedIterator<>(data, offset, maxIndex);
//
//        if(priorIt == null) {
//            logger.debug("cache iteration complete");
//        } else {
//            logger.debug("cache iteration from [" + priorIt.getOffset() + "," + priorIt.getMaxIndex() + ")");
//        }
//
//        return priorIt;
//    }
//
////    @Override
////    public T computeNext() {
////        T data = cache.getData();
////
////        T result;
////        for(;;) {
////
////            // TODO Get rid of needless synchronization for each item
////            synchronized(cache) {
////                if(offset < data.size()) {
////                    result = data.get(offset);
////                    ++offset;
////                    break;
////                } else {
////    //            } else if(cache.isComplete() || cache.isAbanoned()) {
////    //                result = endOfData();
////    //                break;
////    //            } else {
////                    //throw new IndexOutOfBoundsException();
////    //            } else if(cache.isAbanoned()) {
////                    //throw new RuntimeException("Cache was abandoned");
////                            // Re-check whether the cache has been completed
////                    if(cache.isComplete() || cache.isAbandoned()) {
////                        result = endOfData();
////                        break;
////                    } else {
////                        try {
////                            cache.wait();
////                        } catch (InterruptedException e) {
////                            logger.warn("Exception", e);
////                        }
////                    }
////                }
////            }
////        }
////
////        return result;
////    }
//}
