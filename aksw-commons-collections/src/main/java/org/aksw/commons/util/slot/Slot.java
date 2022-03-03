package org.aksw.commons.util.slot;

import java.util.function.Supplier;

public interface Slot<P>
    extends AutoCloseable
{
    Slot<P> setSupplier(Supplier<P> partSupplier);
    Supplier<P> getSupplier();

    // TODO Rename to set/getValue?
    default P get() {
        Supplier<P> supplier = getSupplier();
        P result = supplier == null ? null : supplier.get();
        return result;
    }

    default Slot<P> set(P part) {
        return setSupplier(() -> part);
    }

    @Override
    void close();
}