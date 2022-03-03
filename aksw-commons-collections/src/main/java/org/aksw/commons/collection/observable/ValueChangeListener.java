package org.aksw.commons.collection.observable;

public interface ValueChangeListener<T> {
    void propertyChange(ValueChangeEvent<T> evt);

}
