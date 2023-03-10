package org.aksw.commons.util.range;

/**
 * Interface for abstracting comparison of values.
 * Only one of the methods {@link #isMin()}, {@link #isMax()}, or {@link #hasValue()} may return true on any instance.
 */
public interface Cmp<T>
    extends Comparable<Cmp<T>>
{
    /** Whether this cmp instance is the minimum. compareTo(min, x) returns 0 iff x is min and -1 otherwise */
    boolean isMin();

    /** Whether this cmp instance is the maximum. compareTo(max, x) returns 0 iff x is ax and 1 otherwise */
    boolean isMax();

    /** Whether this cmp instance carries a value. */
    boolean hasValue();

    /** Get the value of this cmp. Raises an exception if there is no value. */
    T getValue();
}
