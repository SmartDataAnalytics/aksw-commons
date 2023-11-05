package org.aksw.commons.util.function;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A predicate to test containment in the backing collection.
 * Allows for casting and unwrapping the items.
 */
public class PredicateContains<T>
    implements Predicate<T>, Serializable
{
    private static final long serialVersionUID = 1L;
    protected Collection<T> items;

    protected PredicateContains(Collection<T> items) {
        super();
        this.items = Objects.requireNonNull(items);
    }

    public static <T> Predicate<T> of(T item) {
        return of(Set.of(item));
    }

    public static <T> Predicate<T> of(Collection<T> items) {
        return new PredicateContains<>(items);
    }

    public Collection<T> getItems() {
        return items;
    }

    @Override
    public boolean test(T item) {
        boolean result = items.contains(item);
        return result;
    }

    @Override
    public String toString() {
        return "test if x in " + items;
    }


    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PredicateContains<?> other = (PredicateContains<?>) obj;
        return Objects.equals(items, other.items);
    }
}
