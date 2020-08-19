package org.aksw.commons.collections;

import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.function.Predicate;

public class FilteringListIterator<T, I extends ListIterator<T>>
    implements ListIterator<T>
{
    protected I core;
    protected Predicate<? super T> predicate;
    protected int currentIndex;

    protected boolean wasPreviousOrNextCalled = false;

    public void setWasPreviousOrNextCalled(boolean flag) {
        this.wasPreviousOrNextCalled = flag;
    }

    public FilteringListIterator(I core, Predicate<? super T> predicate, int currentIndex) {
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
        ListIteratorUtils.repeatPrevious(core, distance);
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
        ListIteratorUtils.repeatNext(core, distance);

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
        if(!predicate.test(e)) {
            throw new IllegalArgumentException("Failed to add item because of rejection by filter. Item: " + e);
        }

        core.add(e);
    }
}