package org.aksw.commons.util.slot;

import java.util.function.Supplier;

public interface SlotDelegate<T>
    extends Slot<T>
{
    Slot<T> getDelegate();

    @Override
    default Slot<T> setSupplier(Supplier<T> partSupplier) {
        getDelegate().setSupplier(partSupplier);
        return this;
    }

    @Override
    default Supplier<T> getSupplier() {
        return getDelegate().getSupplier();
    }

    @Override
    default void close() {
        getDelegate().close();
    }
}
