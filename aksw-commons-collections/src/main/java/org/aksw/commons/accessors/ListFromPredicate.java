package org.aksw.commons.accessors;

import java.util.AbstractList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import com.google.common.collect.Iterators;

public class ListFromPredicate<T>
    extends AbstractList<T>
{
    protected List<T> backend;
    protected Predicate<? super T> predicate;

    public static class ListIteratorFromPredicate<T, I extends ListIterator<T>>
        implements ListIterator<T>
    {
        protected I core;
        protected Predicate<? super T> predicate;
        protected int currentIndex;

        protected boolean wasPreviousOrNextCalled = false;

        public void setWasPreviousOrNextCalled(boolean flag) {
            this.wasPreviousOrNextCalled = flag;
        }

        public ListIteratorFromPredicate(I core, Predicate<? super T> predicate, int currentIndex) {
            super();
            this.core = core;
            this.predicate = predicate;
            this.currentIndex = currentIndex;
        }

        public static void checkDistance(int distance) {
            if(distance == 0) {
                throw new NoSuchElementException();
            }
        }

        @Override
        public boolean hasNext() {
            int distance = ListIteratorUtils.distanceToNext(core, predicate).getKey();
            ListIteratorUtils.previous(core, distance);
            boolean result = distance > 0;
            return result;
        }

        @Override
        public T next() {
            wasPreviousOrNextCalled = true;

            Entry<Integer, T> e = ListIteratorUtils.distanceToNext(core, predicate);
            int distance = e.getKey();
            checkDistance(distance);

            ++currentIndex;
            T result = e.getValue();
            return result;
        }

        @Override
        public void remove() {
            if(!wasPreviousOrNextCalled) {
                throw new IllegalStateException(".remove() requires positioning on a valid element using .previous() or .next()");
            }

            core.remove();
            wasPreviousOrNextCalled = false;
        }

        @Override
        public boolean hasPrevious() {
            int distance = ListIteratorUtils.distanceToPrevious(core, predicate).getKey();
            ListIteratorUtils.next(core, distance);

            boolean result = distance > 0;
            return result;
        }

        @Override
        public T previous() {
            wasPreviousOrNextCalled = true;

            Entry<Integer, T> e = ListIteratorUtils.distanceToPrevious(core, predicate);
            int distance = e.getKey();
            checkDistance(distance);

            --currentIndex;
            T result = e.getValue();
            return result;
        }

        @Override
        public int nextIndex() {
            int distance = ListIteratorUtils.distanceToNext(core, predicate).getKey();
            int result = distance > 0 ? currentIndex + 1 : currentIndex;
            return result;
        }

        @Override
        public int previousIndex() {
            int distance = ListIteratorUtils.distanceToPrevious(core, predicate).getKey();
            int result = distance > 0 ? currentIndex - 1 : currentIndex;
            return result;
        }

        @Override
        public void set(T e) {
            core.set(e);
        }

        @Override
        public void add(T e) {
            core.add(e);
        }
    };




    public ListFromPredicate(List<T> backend, Predicate<? super T> predicate) {
        super();
        this.backend = backend;
        this.predicate = predicate;
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        ListIterator<T> core = backend.listIterator();
        ListIteratorFromPredicate<T, ListIterator<T>> result = new ListIteratorFromPredicate<T, ListIterator<T>>(core, predicate, index - 1);

        // reachedIndex should always match .size()
        int reachedIndex = ListIteratorUtils.next(result, index);

        if(index > reachedIndex) {
            throw new IndexOutOfBoundsException("Requested index " + index + " in list of size " + reachedIndex);
        }

        result.setWasPreviousOrNextCalled(false);

        return result;
    }

    @Override
    public void add(int index, T element) {
        ListIterator<T> it = listIterator(index);
        it.add(element);
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
}
