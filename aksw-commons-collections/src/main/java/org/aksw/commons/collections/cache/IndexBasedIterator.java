package org.aksw.commons.collections.cache;

import java.util.Iterator;
import java.util.List;

public class IndexBasedIterator<T>
    implements Iterator<T>
{
    protected List<T> list;
    protected int offset;

    protected int maxIndex;

    public IndexBasedIterator(List<T> list, int maxIndex) {
        this(list, 0, maxIndex);
    }

    public IndexBasedIterator(List<T> list, int offset, int maxIndex) {
        super();
        this.list = list;
        this.offset = offset;
        this.maxIndex = maxIndex;
    }

    @Override
    public boolean hasNext() {
        boolean result = offset < maxIndex;
        return result;
    }

    @Override
    public T next() {
        T result = list.get(offset++);
        return result;
    }

    public int getOffset() {
        return offset;
    }

    public int getMaxIndex() {
        return maxIndex;
    }

    @Override
    public String toString() {
        return "IndexBasedIterator [list=" + list + ", offset=" + offset + ", maxIndex=" + maxIndex + "]";
    }
}
