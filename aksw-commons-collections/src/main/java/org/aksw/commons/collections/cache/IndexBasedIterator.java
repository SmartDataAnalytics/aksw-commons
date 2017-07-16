package org.aksw.commons.collections.cache;

import java.util.Iterator;
import java.util.List;

public class IndexBasedIterator<T>
    implements Iterator<T>
{
    protected List<T> list;
    protected int offset;

    public IndexBasedIterator(List<T> list) {
        this(list, 0);
    }

    public IndexBasedIterator(List<T> list, int offset) {
        super();
        this.list = list;
        this.offset = offset;
    }

    /**
     * Simply try to access an element by index.
     * This way, a lazy loading list can block the call to .get() until it knows whether there is sufficient data.
     */
    @Override
    public boolean hasNext() {
        boolean result;
        try {
            list.get(offset);
            result = true;
        } catch(IndexOutOfBoundsException e) {
            result = false;
        }

        return result;
    }

    @Override
    public T next() {
        T result = list.get(offset);
        ++offset;
        return result;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "IndexBasedIterator [list=" + list + ", offset=" + offset + "]";
    }
}
