package org.aksw.commons.collections.lists;

import java.util.Iterator;

public class LinkedListIterator<T>
    implements Iterator<T>
{
    protected LinkedListNode<T> current;

    public LinkedListIterator(LinkedListNode<T> current) {
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