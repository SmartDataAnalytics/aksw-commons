package org.aksw.commons.collections;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

import com.google.common.collect.Iterators;

public class FilteringCollection<T, C extends Collection<T>>
    extends AbstractCollection<T>
{
    protected C backend;
    protected Predicate<? super T> predicate;

    public FilteringCollection(C backend, Predicate<? super T> predicate) {
        super();
        this.backend = backend;
        this.predicate = predicate;
    }

    public C getBackend() {
		return backend;
	}
    
    public Predicate<? super T> getPredicate() {
		return predicate;
	}
    
    @Override
    public boolean add(T e) {
        if(!predicate.test(e)) {
            throw new IllegalArgumentException("add failed because item was rejected by predicate; violating item: " + e);
        }
        boolean result = backend.add(e);
        return result;
    }

    @Override
    public boolean contains(Object o) {
    	boolean result;
    	try {
    		boolean accepted = predicate.test((T)o);
    		
            result = accepted
                    ? backend.contains(o)
                    : false;
    	} catch (ClassCastException e) {
    		result = false;
    	}


        return result;
    }

    @Override
    public boolean remove(Object o) {
    	boolean result;
    	try {
	        boolean accepted = predicate.test((T)o);
	
	        result = accepted
	                ? backend.remove(o)
	                : false;
    	} catch (ClassCastException e) {
    		result = false;
    	}
    	
        return result;
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> baseIt = backend.iterator();

        return new FilteringIterator<>(baseIt, predicate);
    }

    @Override
    public int size() {
        int result = Iterators.size(iterator());
        return result;
    }
}
