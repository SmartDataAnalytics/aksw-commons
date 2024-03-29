package org.aksw.commons.collection.observable;

import java.util.Collection;
import java.util.Collections;

public class CollectionChangedEventImpl<T>
    extends CollectionChangedEvent<T>
{
    protected Collection<T> additions;
    protected Collection<T> deletions;
    protected Collection<T> refreshes;

//    public CollectionChangedEvent(Object source, String propertyName, Object oldValue, Object newValue) {
//        super(source, propertyName, oldValue, newValue);
//    }

    public CollectionChangedEventImpl(Object source,
            Object oldValue,
            Object newValue,

            Collection<T> additions,
            Collection<T> deletions,
            Collection<T> refreshes) {
        super(source, "items", oldValue, newValue);
        this.additions = additions == null ? Collections.emptySet() : additions;
        this.deletions = deletions == null ? Collections.emptySet() : deletions;
        this.refreshes = refreshes == null ? Collections.emptySet() : refreshes;
    }

    public Collection<T> getAdditions() {
        return additions;
    }

    public Collection<T> getDeletions() {
        return deletions;
    }

    public Collection<T> getRefreshes() {
        return refreshes;
    }

    @Override
    public String toString() {
        return "CollectionChangedEventImpl [additions=" + additions + ", deletions=" + deletions + ", refreshes="
                + refreshes + ", newValue= " + getNewValue() + ", oldValue=" + getOldValue() + "]";
    }
}
