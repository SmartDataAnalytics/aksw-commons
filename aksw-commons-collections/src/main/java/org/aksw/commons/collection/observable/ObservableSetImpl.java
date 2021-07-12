package org.aksw.commons.collection.observable;

import java.util.Set;

public class ObservableSetImpl<T>
    extends ObservableCollectionBase<T, Set<T>>
    implements ObservableSet<T>
{
    public ObservableSetImpl(Set<T> decoratee) {
        super(decoratee);
    }

    public static <T> ObservableSet<T> decorate(Set<T> decoratee) {
        return new ObservableSetImpl<T>(decoratee);
    }
}
