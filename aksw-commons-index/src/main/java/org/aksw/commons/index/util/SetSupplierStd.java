package org.aksw.commons.index.util;

import java.util.Set;

public interface SetSupplierStd
    extends SetSupplier<Set<?>>
{
    @Override
    <T> Set<T> get();


    /**
     * Wrap this map supplier such that any supplied map becomes wrapped
     * as a cmap.
     *
     * @return
     */
    default SetSupplierCSet wrapAsCSet() {
        SetSupplierStd self = this;
        return new SetSupplierCSet() {
            @Override
            public <T> CSet<T> get() {
                Set<T> tmp = self.<T>get();
                return new CSetImpl<T>(tmp);
            }
        };
    }
}
