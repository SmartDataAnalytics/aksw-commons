package org.aksw.commons.collection.observable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

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
    protected VetoableChangeSupport vcs = new VetoableChangeSupport(this);
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public ObservableValueImpl(SingleValuedAccessor<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void set(T value) {
        T before = delegate.get();        
        try {
			vcs.fireVetoableChange(new PropertyChangeEvent(this, "value", before, value));
		} catch (PropertyVetoException e) {
			throw new RuntimeException(e);
		}
        delegate.set(value);
        pcs.firePropertyChange(new PropertyChangeEvent(this, "value", before, value));
    }

    @Override
    public T get() {
        T result = delegate.get();
        return result;
    }

    @Override
    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
        return () -> pcs.removePropertyChangeListener(listener);
    }

    @Override
    public Runnable addVetoableChangeListener(VetoableChangeListener listener) {
        vcs.addVetoableChangeListener(listener);
        return () -> vcs.removeVetoableChangeListener(listener);
    }

    public static <T> ObservableValue<T> create(T initialValue) {
    	return new ObservableValueImpl<>(new SingleValuedAccessorDirect<>(initialValue));
    }
}
