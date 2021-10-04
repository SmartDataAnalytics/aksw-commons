package org.aksw.commons.collection.observable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public abstract class ForwardingDeltaCollectionBase<T, C extends Collection<T>>
    extends DeltaCollectionBase<T, Collection<T>>
{
    protected C backend;

    public ForwardingDeltaCollectionBase(C backend) {
        this.backend = backend;
    }

    public Collection<T> getBackend() {
        return backend;
    }

    protected boolean isDuplicateAwareBackend() {
        return !(backend instanceof Set);
    }

    @Override
    public Iterator<T> iterator() {
        return backend.iterator();
    }

    @Override
    public int size() {
        return backend.size();
    }

}
