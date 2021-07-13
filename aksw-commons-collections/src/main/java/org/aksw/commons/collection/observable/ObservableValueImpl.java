package org.aksw.commons.collection.observable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.commons.accessors.SingleValuedAccessorDirect;

/**
 * Decorates a {@link SingleValuedAccessor} (a getter+setter interface) with property change support.
 *
 * @author raven
 *
 * @param <T>
 */
public class ObservableValueImpl<T>
    implements SingleValuedAccessor<T>, ObservableValue<T>
{
    protected SingleValuedAccessor<T> delegate;
    protected PropertyChangeSupport pce = new PropertyChangeSupport(this);

    public ObservableValueImpl(SingleValuedAccessor<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void set(T value) {
        T before = delegate.get();
        pce.firePropertyChange(new PropertyChangeEvent(this, "value", before, value));
        delegate.set(value);
    }

    @Override
    public T get() {
        T result = delegate.get();
        return result;
    }

    @Override
    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        pce.addPropertyChangeListener(listener);
        return () -> pce.removePropertyChangeListener(listener);
    }
    
    public static <T> ObservableValue<T> create(T initialValue) {
    	return new ObservableValueImpl<>(new SingleValuedAccessorDirect<>(initialValue));
    }
}