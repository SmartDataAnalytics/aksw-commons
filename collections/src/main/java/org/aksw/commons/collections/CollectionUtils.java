package org.aksw.commons.collections;

import java.util.Collections;
import java.util.Iterator;

/**
 * User: raven
 * Date: 4/17/11
 * Time: 12:36 AM
 */
public class CollectionUtils {

    /**
     * Given an iterable A whose elements are iterables, this method will return the first
     * element of A.
     * If no such element exist, an empty iterable is returned rather than null.
     * 
     *
     * @param iterable
     * @param <S>
     * @param <T>
     * @return
     */
    public static <S, T extends Iterable<S>> Iterable<S> safeGetFirst(Iterable<T> iterable) {
        Iterator<T> it = iterable.iterator();

        return it.hasNext() ? it.next() : Collections.<S>emptySet();
    }
}
