package org.aksw.commons.collections.lists;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;



/**
 * A linked list node used in the combinatoric stream
 *
 * @author raven
 *
 * @param <T>
 */
public class LinkedListNode<T>
    implements Iterable<T>
{
    public T data;
    public LinkedListNode<T> predecessor;
    public LinkedListNode<T> successor;

    public void append(LinkedListNode<T> node) {
        successor = node;
        node.predecessor = this;
    }

    public void unlink() {
        predecessor.successor = successor;
        successor.predecessor = predecessor;
    }

    public void relink() {
        successor.predecessor = this;
        predecessor.successor = this;
    }

//    boolean isEmpty() {
//        boolean result = predecessor == null && successor == null;
//        return result;
//    }
    public boolean isHead() {
        boolean result = predecessor == null;
        return result;
    }

    public boolean isTail() {
        boolean result = successor == null;
        return result;
    }

    public boolean isFirst() {
        boolean result = predecessor.isHead();
        return result;
    }

    public boolean isLast() {
        boolean result = successor.isTail();
        return result;
    }

    public List<T> toList() {
        List<T> result = new ArrayList<T>();
        LinkedListNode<T> curr = this;
        while(!curr.isTail()) {
            result.add(curr.data);
            curr = curr.successor;
        }
        return result;
    }

    @Override
    public String toString() {
        String result = Iterables.toString(this);
        return result;
    }

    public static <S> LinkedListNode<S> create(Iterable<S> it) {
        LinkedListNode<S> head = new LinkedListNode<S>();
        head.data = null;

        LinkedListNode<S> curr = head;
        for(S item : it) {
            LinkedListNode<S> next = new LinkedListNode<S>();
            next.data = item;
            curr.append(next);
            curr = next;
        }

        LinkedListNode<S> tail = new LinkedListNode<S>();
        curr.append(tail);

        return head;
    }

    /**
     * Iterator over the items in the linked list
     */
    @Override
    public Iterator<T> iterator() {
        Iterator<T> result = isHead()
                ? new LinkedListIterator<>(this.successor)
                : new LinkedListIterator<>(this);

        return result;
    }

    /**
     * Iterator over the node objects (of which each holds an item) in the list
     * 
     * @return
     */
    public Iterator<LinkedListNode<T>> nodeIterator() {
        LinkedListNodeIterator<T> result = isHead()
                ? new LinkedListNodeIterator<>(this.successor)
                : new LinkedListNodeIterator<>(this);

        return result;
    }


}