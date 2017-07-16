package org.aksw.commons.collections.cache;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

/**
 * An iterable that caches (possibly computed) items returned from an underlying iterator, such that
 * subsequent iterations run from the cache.
 *
 * Useful for constructing cartesian products on-demand
 *
 * @author raven
 *
 * @param <T>
 */

public class CacheImpl<T>
    //extends AbstractCollection<T>
    extends AbstractList<T>
    implements Cache<T>
//    implements CacheX<T>
{
    //protected L data = new ArrayList<T>();
    protected List<T> data;
    boolean isComplete = false;

    //protected Set<Iterator<T>> weakHashSet = Collections.newSetFromMap(new WeakHashMap<Iterator<T>, Boolean>());

    // Setting this flag is only valid if the cache is not completed yet
    // It indicates that no further items can be expected to be added to the cache
    // Hence, any blocking client should no longer wait for it but fail with an exception
    boolean isAbandoned = false;

    public CacheImpl(List<T> data) {
        this.data = data;
    }

    public List<T> getData() {
        return data;
    }

    public synchronized boolean isComplete() {
        return isComplete;
    }

    public synchronized void setComplete(boolean status) {
        this.isComplete = status;
        notifyAll();
    }

    public synchronized boolean isAbandoned() {
        return isAbandoned;
    }

    public synchronized void setAbandoned(boolean isAbandoned) {
        this.isAbandoned = isAbandoned;
        notifyAll();
    }

    @Override
    public synchronized boolean add(T e) {
        if(isComplete()) {
            throw new RuntimeException("Cannot add data to completed cache");
        }

        boolean result = data.add(e);
        notifyAll();
        return result;
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> result = new IndexBasedIterator<>(this);
        return result;
    }


    @Override
    public synchronized int size() {
        // Wait until the data is complete
        synchronized(this) {
            while(!isComplete() && !isAbandoned()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }

        if(isAbandoned()) {
            throw new RuntimeException("Collection was abandoned");
        }

        // Implicit isComplete() here
        int result = data.size();
        return result;
    }

    /**
     * Size returns the current number of items in the cache
     */
    @Override
    public synchronized int getCurrentSize() {
        int result = data.size();
        return result;
    }


    @Override
    public void setComplete() {
        setComplete(true);
    }

    @Override
    public void setAbandoned() {
        setAbandoned(true);
    }

    /**
     * A call to get blocks until the cache is complete
     */
    @Override
    public T get(int index) {

        // Wait until the cache is abandoned, or more data to becomes available or the cache is complete
        int maxIndex;
        synchronized(this) {
            while(index >= (maxIndex = getCurrentSize()) && !isComplete() && !isAbandoned()) {
                try {
                    wait();
                } catch(InterruptedException e) {
                    //break;
                }
            }
        }

        T result;
        if(index <= maxIndex) {
            result = data.get(index);
        } else {
            throw new IndexOutOfBoundsException(index + " >= " + maxIndex);
        }

        return result;

        // Create an iterator up to the size of the data
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
    }

    @Override
    public synchronized void close() throws Exception {
        if(!isComplete()) {
            setAbandoned();
        }
    }



//    public int size() {
//        int result = data.size();
//        return result;
//    }
}