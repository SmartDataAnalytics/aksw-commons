package org.aksw.commons.util.algebra;

public interface ExprFilter<E> {
    boolean test(E parent, int childIdx, E child);
}
