package org.aksw.commons.collection.observable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Objects;

import com.google.common.base.Converter;

public class ObservableConvertingValue<F, B>
    implements ObservableValue<F>
{
    protected ObservableValue<B> delegate;
    protected Converter<B, F> converter;

    public ObservableConvertingValue(ObservableValue<B> delegate, Converter<B, F> converter) {
        super();
        this.delegate = delegate;
        this.converter = converter;
    }

    @Override
    public F get() {
        B b = delegate.get();
        F result = converter.convert(b);
        return result;
    }

    @Override
    public void set(F value) {
        B b = converter.reverse().convert(value);
        delegate.set(b);
    }

    @Override
    public Registration addPropertyChangeListener(PropertyChangeListener listener) {
        return delegate.addPropertyChangeListener(ev -> {
            B rawOldValue = (B)ev.getOldValue();
            B rawNewValue = (B)ev.getNewValue();

            F oldValue = converter.convert(rawOldValue);
            F newValue = converter.convert(rawNewValue);

            if (oldValue != null && newValue != null && !Objects.equals(oldValue, newValue)) {
                listener.propertyChange(new PropertyChangeEvent(this, "value", oldValue, newValue));
            }
        });
    }

    @Override
    public Runnable addVetoableChangeListener(VetoableChangeListener listener) {
        throw new UnsupportedOperationException();
    }

}
