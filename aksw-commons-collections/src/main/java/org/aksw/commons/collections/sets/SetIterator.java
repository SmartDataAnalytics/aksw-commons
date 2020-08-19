package org.aksw.commons.collections.sets;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.aksw.commons.collections.SinglePrefetchIterator;

public class SetIterator<T>
    extends SinglePrefetchIterator<T>
{
    protected Collection<T> backend;

    public SetIterator(Collection<T> backend) {
        this.backend = backend;
    }

    protected Set<T> seen = new HashSet<>();
    protected Iterator<T> current = null;

    @Override
    protected T prefetch() throws Exception {
        if(current == null) {
            current = backend.iterator();
        }

        T result;
        while(current.hasNext()) {
            result = current.next();

            if(!seen.contains(result)) {
                seen.add(result);
                return result;
            }
        }

        current = null; // Let's be polite and clean up the reference
        return finish();
    }

    @Override
    protected void doRemove(T item) {
        while(backend.remove(item)) {
            /* remove until no more change */
        }
        // Invalidate the current iterator
        // The next call to .prefetch() will position the underlying iterator to the
        // first item not in the set of seen items
        current = null;
    }
}