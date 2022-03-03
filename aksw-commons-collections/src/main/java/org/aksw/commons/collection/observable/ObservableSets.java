package org.aksw.commons.collection.observable;

public class ObservableSets {
    public static <T> ObservableSet<T> union(ObservableSet<T> a, ObservableSet<T> b) {
        return ObservableSetUnion.create(a, b);
    }

    public static <T> ObservableSet<T> difference(ObservableSet<T> a, ObservableSet<T> b) {
        return ObservableSetDifference.create(a, b);
    }
}
