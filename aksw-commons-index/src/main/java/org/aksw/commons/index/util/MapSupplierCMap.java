package org.aksw.commons.index.util;

public interface MapSupplierCMap
    extends MapSupplier<CMap<?, ?>>
{
    @Override
    <K, V> CMap<K, V> get();
}
