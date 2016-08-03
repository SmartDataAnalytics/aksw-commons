package org.aksw.commons.collections.tracker;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.aksw.commons.collections.diff.SetDiff;

/**
 * Set which tracks insertions and removals to another set
 * 
 * Can be used with maps by applying it to their .entrySet()
 * 
 * @author raven
 *
 */
public class TrackingSet<T>
    extends AbstractSet<T>
{
    protected SetDiff<T> diff;
    protected Set<T> delegate;

    public TrackingSet(Set<T> delegate) {
        this.delegate = delegate;
        this.diff = new SetDiff<>(new HashSet<T>(), new HashSet<T>(), null);
    }
    
    public SetDiff<T> getDiff() {
        return diff;
    }
    
    public void restore() {
        delegate.removeAll(diff.getAdded());
        delegate.addAll(diff.getRemoved());
    }
    
    @Override
    public boolean add(T e) {
        boolean result = delegate.add(e);
        if(result) {
            // If the object was in the removed set, it means that
            // its addition restored the original state
            if(diff.getRemoved().contains(e)) {
                diff.getRemoved().remove(e);
            } else {
                diff.getAdded().add(e);                
            }
        }

        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        boolean result = delegate.remove(o);
        if(result) {
            if(diff.getAdded().contains(o)) {
                diff.getAdded().remove(o);
            } else {
                diff.getRemoved().add((T)o);
            }
        }

        return result;
    }
    
    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }
}
