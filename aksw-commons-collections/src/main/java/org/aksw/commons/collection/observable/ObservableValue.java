package org.aksw.commons.collection.observable;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;

import org.aksw.commons.accessors.SingleValuedAccessor;

import com.google.common.base.Converter;

public interface ObservableValue<T>
    extends SingleValuedAccessor<T>
{
    Registration addPropertyChangeListener(PropertyChangeListener listener);
    Runnable addVetoableChangeListener(VetoableChangeListener listener);

    /** Gets notified before {@link #get()} changes to the new value */
    default Runnable addVetoableChangeListener(ValueChangeListener<T> listener) {
        VetoableChangeListener fn = ev -> listener.propertyChange(ValueChangeEvent.<T>adapt(ev));
        return addVetoableChangeListener(fn);
    }

    /** Type-safe variant. Uses {@link #addPropertyChangeListener(PropertyChangeListener)} and casts. */
    default Registration addValueChangeListener(ValueChangeListener<T> listener) {
        return addPropertyChangeListener(ev -> listener.propertyChange(ValueChangeEvent.<T>adapt(ev)));
    }

    default <X> ObservableValue<X> convert(Converter<T, X> converter) {
        return new ObservableConvertingValue<>(this, converter);
    }
}
