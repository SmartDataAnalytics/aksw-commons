package org.aksw.commons.collections;

import java.util.Iterator;

/**
 * Created by Claus Stadler
 * Date: Nov 1, 2010
 * Time: 10:22:25 PM
 */
public class IteratorUtils {
    public static <T> Iterable<T> makeIterable(Iterator<T> iterator) {
        return new IteratorIterable<T>(iterator);
    }
}
