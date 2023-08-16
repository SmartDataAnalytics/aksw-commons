package org.aksw.commons.collections.lists;

import java.util.ListIterator;
import java.util.Objects;

/** ListIterator wrapper that delegates next() calls to the delegate.previous() and vice versa. */
public class ReverseListIterator<T>
    implements ListIterator<T>
{
    protected ListIterator<T> delegate;

    public ReverseListIterator(ListIterator<T> delegate) {
        super();
        this.delegate = Objects.requireNonNull(delegate);
    }

    public static <T> ListIterator<T> of(ListIterator<T> it) {
        return new ReverseListIterator<>(it);
    }
    protected ListIterator<T> getDelegate() {
        return delegate;
    }

    @Override
    public boolean hasNext() {
        return getDelegate().hasPrevious();
    }

    @Override
    public T next() {
        return getDelegate().previous();
    }

    @Override
    public boolean hasPrevious() {
        return getDelegate().hasNext();
    }

    @Override
    public T previous() {
        return getDelegate().next();
    }

    @Override
    public int nextIndex() {
        return getDelegate().previousIndex();
    }

    @Override
    public int previousIndex() {
        return getDelegate().nextIndex();
    }

    @Override
    public void remove() {
        ListIterator<T> d = getDelegate();
        d.remove();
    }

    @Override
    public void set(T e) {
        getDelegate().add(e);
    }

    @Override
    public void add(T e) {
        ListIterator<T> d = getDelegate();
        d.add(e);

        // Go back to previous:
        // ListIterator contract states that previous() must return a just added item.
        // So given the sequence "it.add(x); r = it.previous()" it must hold that "r == x".
        d.previous();
    }
}
