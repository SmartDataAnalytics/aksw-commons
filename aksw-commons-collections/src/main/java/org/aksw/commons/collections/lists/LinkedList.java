package org.aksw.commons.collections.lists;

import java.util.AbstractList;
import java.util.Iterator;

public class LinkedList<T>
    extends AbstractList<T>
{
    protected LinkedListNode<T> first;
    protected LinkedListNode<T> last;
    protected int size;

    public LinkedList() {
        this.first = new LinkedListNode<>();
        this.last = new LinkedListNode<>();
//        this.size = 0;

        first.successor = last;
        last.predecessor = first;        
    }
    
    public LinkedListNode<T> append(T item) {
        LinkedListNode<T> result = new LinkedListNode<>();
        result.data = item;
        
        LinkedListNode<T> prev = last.predecessor; 
        
        prev.successor = result;
        
        result.predecessor = prev;
        result.successor = last;
        
        last.predecessor = result;
        
        return result;
    }
    
    @Override
    public boolean add(T item) {
        append(item);
        
        return true;
    }
        
    @Override
    public T get(int index) {
        LinkedListNode<T> curr = first.successor;
        
        for(int i = 0; i < index; ++i) {
            curr = curr.successor;
        }
        
        T result = curr.data;
        return result;
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> result = first.iterator();
        return result;
    }
    
    /**
     * TODO: Ideally the complexity of the size function should be O(1) instead O(n)
     * But this would mean, that certain functions (link / unlink) that are currently
     * part of LinkedListNode would have to go here instead.
     */
    @Override
    public int size() {
        int result = 0;
        LinkedListNode<T> curr = first.successor;
        
        while(curr != last) {
            curr = curr.successor;
            ++result;
        }

        return result;
    }
}
