package org.aksw.commons.collection.observable;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Abstract base implementation of {@link DeltaCollection} that delegates all mutation
 * methods to {@link #delta(Collection, Collection)}.
 *
 * @author raven
 *
 * @param <T>
 * @param <C>
 */
public abstract class DeltaCollectionBase<T, C extends Collection<T>>
    extends AbstractCollection<T>
    implements DeltaCollection<T>
{
    @Override
    public boolean add(T value) {
        return addAll(Collections.singleton(value));
    }

    @Override
    public boolean addAll(Collection<? extends T> addedItems) {
        return delta(addedItems, Collections.emptySet());

    }

    @Override
    public void clear() {
        removeAll(this);
    }

    @Override
    public boolean remove(Object o) {
        return removeAll(Collections.singleton(o));
    }


    @Override
    public boolean removeAll(Collection<?> c) {
        return delta(Collections.emptySet(), c);
    }
}
