package org.aksw.commons.collections;

import java.util.Iterator;

public class LinkedListNodeIterator<T>
    implements Iterator<T>
{
    protected LinkedListNode<T> current;

    public LinkedListNodeIterator(LinkedListNode<T> current) {
        super();
        this.current = current;
    }

    @Override
    public boolean hasNext() {
        boolean result = !current.isTail();
        return result;
    }

    @Override
    public T next() {
        T result = current.data;
        current = current.successor;
        return result;
    }
}