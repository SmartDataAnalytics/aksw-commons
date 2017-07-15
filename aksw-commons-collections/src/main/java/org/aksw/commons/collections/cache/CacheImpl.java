package org.aksw.commons.collections.cache;

import java.util.AbstractCollection;
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
    extends AbstractCollection<T>
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
        boolean result = data.add(e);
        notifyAll();
        return result;
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> result = new BlockingCacheIterator<>(this);
        return result;
    }


    /**
     * Size returns the current number of items in the cache
     */
    @Override
    public synchronized int size() {
        int result = data.size();
        return result;
    }

    @Override
    public void setComplete() {
        setComplete(true);
    }


//    public int size() {
//        int result = data.size();
//        return result;
//    }
}