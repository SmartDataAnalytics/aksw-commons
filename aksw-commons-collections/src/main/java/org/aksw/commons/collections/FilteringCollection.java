package org.aksw.commons.collections;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

import com.google.common.collect.Iterators;

public class FilteringCollection<T, C extends Collection<T>>
    extends AbstractCollection<T>
{
    protected C backend;
    protected Predicate<Object> predicate;

    public FilteringCollection(C backend, Predicate<Object> predicate) {
        super();
        this.backend = backend;
        this.predicate = predicate;
    }

    @Override
    public boolean add(T e) {
        if(!predicate.test(e)) {
            throw new IllegalArgumentException("add failed because item was rejected by predicate " + e);
        }
        boolean result = backend.add(e);
        return result;
    }

    @Override
    public boolean contains(Object o) {
        boolean accepted = predicate.test(o);

        boolean result = accepted
                ? backend.contains(o)
                : false;

        return result;
    }

    @Override
    public boolean remove(Object o) {
        boolean accepted = predicate.test(o);

        boolean result = accepted
                ? backend.remove(o)
                : false;

        return result;
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> baseIt = backend.iterator();

        return new FilteredIterator<>(baseIt, predicate);
    }

    @Override
    public int size() {
        int result = Iterators.size(iterator());
        return result;
    }
}
