package org.aksw.commons.collections;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

import com.google.common.base.Converter;
import com.google.common.collect.Iterators;

public class FilteringList<T, C extends List<T>>
    extends AbstractList<T>
{
    protected C backend;
    protected Predicate<? super T> predicate;

    public FilteringList(C backend, Predicate<? super T> predicate) {
        super();
        this.backend = backend;
        this.predicate = predicate;
    }

    @Override
    public Iterator<T> iterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        ListIterator<T> core = backend.listIterator();
        FilteringListIterator<T, ListIterator<T>> result = new FilteringListIterator<>(core, predicate, index - 1);

        // The highest possible value for reachedIndex should be .size()
        int reachedIndex = ListIteratorUtils.repeatNext(result, index);

        if(index > reachedIndex) {
            throw new IndexOutOfBoundsException("Requested index " + index + " in list of size " + reachedIndex);
        }

        result.setWasPreviousOrNextCalled(false);

        return result;
    }

    @Override
    public boolean add(T e) {
        // TODO super.add always iterates the whole list. Optimize by scanning the underlying iterator from the end.
        return super.add(e);
    }

    @Override
    public void add(int index, T element) {
        ListIterator<T> it = listIterator(index);
        it.add(element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        int size = size();
        return addAll(size, c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean result = !c.isEmpty();
        if (result) {
            ListIterator<T> it = listIterator(index);
            for (T item : c) {
                it.add(item);
            }
        }
        return result;
    }

    @Override
    public T set(int index, T element) {
        ListIterator<T> it = listIterator(index);
        it.next();
        it.set(element);

        return element;
    }

    @Override
    public T get(int index) {
        ListIterator<T> it = listIterator(index);
        T result = it.next();
        return result;
    }

    @Override
    public int size() {
        int result = Iterators.size(listIterator());
        return result;
    }

    /**
     * Wraps the backend with a guava filteredCollection that filters out
     * items for which the converter raises an exception
     *
     * @param <F>
     * @param <B>
     * @param <C>
     * @param backend
     * @param converter
     * @return
     */
    public static <F, B, C extends Collection<B>> Collection<F> createSafe(Collection<B> backend, Converter<B, F> converter) {
        Collection<B> safeBackend = MutableCollectionViews.filteringCollection(backend, converter);
        Collection<F> result = new ConvertingCollection<>(safeBackend, converter);
        return result;
    }
}
