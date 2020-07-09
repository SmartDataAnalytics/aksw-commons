package org.aksw.commons.collections.sets;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Iterators;

public class SetFromCollection<T>
    extends AbstractSet<T>
{
    protected Collection<T> backend;

    public SetFromCollection(Collection<T> backend) {
        super();
        this.backend = backend;
    }


    @Override
    public boolean add(T e) {
        boolean result = backend.contains(e) ? false : backend.add(e);
        return result;
    }

    /**
     * This method removes ALL occurrences of a given item.
     * The backend MUST adhere to the contract that remove()
     * returns false once there is no more change - otherwise
     * calling this method will cause an endless loop.
     *
     */
    @Override
    public boolean remove(Object o) {
        boolean result = false;
        while(backend.remove(o)) {
            /* wait for all elements to have been removed*/
            result = true;
        }
        return result;
    }

    @Override
    public boolean contains(Object o) {
        boolean result = backend.contains(o);
        return result;
    }

    @Override
    public void clear() {
        backend.clear();
    }

    @Override
    public Iterator<T> iterator() {
        SetIterator<T> result = new SetIterator<>(backend);
        return result;
    }

    @Override
    public int size() {
        int result = Iterators.size(iterator());
        //int result = backend.size();
        return result;
    }

    public static <T> Set<T> wrapIfNeeded(Collection<T> collection) {
        Set<T> result = collection instanceof Set ? (Set<T>)collection : new SetFromCollection<>(collection);
        return result;
    }
}
