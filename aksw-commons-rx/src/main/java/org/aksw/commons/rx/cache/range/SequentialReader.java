package org.aksw.commons.rx.cache.range;


/**
 * Conceptually a form of a batch-iterator.
 * Instead of yielding only a single item, an array of items can be retrieved.
 *
 *
 * @author Claus Stadler, Feb 17, 2022
 *
 * @param <A>
 */
public interface SequentialReader<A> {
    int read(A array, int position, int length);
}