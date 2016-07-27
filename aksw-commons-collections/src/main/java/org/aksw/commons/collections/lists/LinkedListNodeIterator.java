package org.aksw.commons.collections.lists;

import java.util.Iterator;

public class LinkedListNodeIterator<T>
    implements Iterator<LinkedListNode<T>>
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
    public LinkedListNode<T> next() {
        LinkedListNode<T> result = current;
        current = current.successor;
        return result;
    }
}