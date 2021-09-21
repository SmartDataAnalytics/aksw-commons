package org.aksw.commons.index.util;

public interface SetSupplierCSet
    extends SetSupplier<CSet<?>>
{
    @Override
    <T> CSet<T> get();
}
