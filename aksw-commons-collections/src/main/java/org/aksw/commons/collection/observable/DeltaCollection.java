package org.aksw.commons.collection.observable;

import java.util.Collection;

/**
 * A delta collection unifies the modification methods add/addAll/remove/removeAll/clear
 * into a single {@link #delta(Collection, Collection)} method.
 *
 * This is very useful as a base for observable collections because any modification can be
 * accomplished using a single method call which then only needs to fire a single
 * {@link CollectionChangedEvent}.
 *
 * @author raven
 *
 * @param <T>
 */
public interface DeltaCollection<T>
    extends Collection<T>
{
    /** Replace the content of this collection with the provided values */
    default boolean replace(Collection<? extends T> newValues) {
        return delta(newValues, this);
    }

    /** Apply a delta thereby firing only a single event */
    boolean delta(Collection<? extends T> additions, Collection<?> removals);
}
