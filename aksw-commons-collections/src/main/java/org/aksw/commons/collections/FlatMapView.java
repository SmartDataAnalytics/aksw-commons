package org.aksw.commons.collections;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Claus Stadler
 * Date: Oct 9, 2010
 * Time: 5:41:56 PM
 */
public class FlatMapView<T>
        extends AbstractCollection<T>
{
    private Collection<? extends Collection<T>> internal;

    public FlatMapView(Collection<? extends Collection<T>> internal) {
        this.internal = internal;
    }


    @Override
    public Iterator<T> iterator() {
        return new ChainIterator<T>(internal.iterator());
    }

    @Override
    public int size() {
        int result = 0;
        for(Collection<T> item : internal) {
            result += item.size();
        }

        return result;
    }
}
