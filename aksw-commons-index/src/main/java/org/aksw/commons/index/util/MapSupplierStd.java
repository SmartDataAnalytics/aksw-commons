package org.aksw.commons.index.util;

import java.util.Map;

public interface MapSupplierStd
    extends MapSupplier<Map<?, ?>>
{
    @Override
    <K, V> Map<K, V> get();

    /**
     * Wrap this map supplier such that any supplied map becomes wrapped
     * as a cmap.
     *
     * @return
     */
    default MapSupplierCMap wrapAsCMap() {
        MapSupplierStd self = this;
        return new MapSupplierCMap() {
            @Override
            public <K, V> CMap<K, V> get() {
                Map<K, V> tmp = self.<K, V>get();
                return new CMapImpl<K, V>(tmp);
            }
        };
    }
}
