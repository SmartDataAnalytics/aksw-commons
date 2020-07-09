package org.aksw.commons.collections;

import java.util.Set;
import java.util.function.Predicate;

public class FilteringSet<T, C extends Set<T>>
    extends FilteringCollection<T, C>
    implements Set<T>
{
    public FilteringSet(C backend, Predicate<Object> predicate) {
        super(backend, predicate);
    }
}
