package org.aksw.commons.collection.observable;

import java.beans.PropertyChangeEvent;
import java.util.Collection;

public abstract class CollectionChangedEvent<T>
    extends PropertyChangeEvent
{
    public CollectionChangedEvent(Object source, String propertyName, Object oldValue, Object newValue) {
        super(source, propertyName, oldValue, newValue);
    }

    public abstract Collection<T> getAdditions();
    public abstract Collection<T> getDeletions();
    public abstract Collection<T> getRefreshes();

    public boolean hasChanges() {
        boolean result = !(getAdditions().isEmpty() && getDeletions().isEmpty() && getRefreshes().isEmpty());
        return result;
    }

    // added items
    // removed items
    // refreshed items
}
