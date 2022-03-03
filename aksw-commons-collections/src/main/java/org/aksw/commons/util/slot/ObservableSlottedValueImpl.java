package org.aksw.commons.util.slot;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.function.Supplier;

import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.collection.observable.ObservableValueImpl;

public class ObservableSlottedValueImpl<W, P>
    extends SlottedBuilderDelegateBase<W, P>
    implements ObservableSlottedValue<W, P>
{
    protected ObservableValue<W> cachedValue;

    public ObservableSlottedValueImpl(SlottedBuilder<W, P> delegate) {
        super(delegate);
        this.cachedValue = ObservableValueImpl.create(null);
        build();
    }

    public static <W, P> ObservableSlottedValue<W, P> wrap(SlottedBuilder<W, P> delegate) {
        return new ObservableSlottedValueImpl<W, P>(delegate);
    }

    @Override
    public W build() {
        W value = getDelegate().build();
        cachedValue.set(value);
        return value;
    }

    @Override
    public W get() {
        return build();
    }

    @Override
    public void set(W value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Slot<P> newSlot() {
        Slot<P> delegate = super.newSlot();

        return new SlotDelegateBase<P>(delegate) {
            @Override
            public Slot<P> setSupplier(Supplier<P> partSupplier) {
                getDelegate().setSupplier(partSupplier);
                build();
                return this;
            }
        };
    }

    @Override
    public Runnable addPropertyChangeListener(PropertyChangeListener listener) {
        return cachedValue.addPropertyChangeListener(listener);
    }

	@Override
	public Runnable addVetoableChangeListener(VetoableChangeListener listener) {
        return cachedValue.addVetoableChangeListener(listener);
	}
	
	@Override
	public String toString() {
		return cachedValue.toString();
	}
}
