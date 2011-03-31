package org.aksw.commons.collections;

import java.util.Iterator;

/**
 * Created by Claus Stadler
 * Date: Nov 1, 2010
 * Time: 10:23:02 PM
 */
public class IteratorIterable<T>
        implements Iterable<T>
{
    private Iterator<T> iterator;

    public IteratorIterable(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    public Iterator<T> iterator()
    {
        return iterator;
    }
}
