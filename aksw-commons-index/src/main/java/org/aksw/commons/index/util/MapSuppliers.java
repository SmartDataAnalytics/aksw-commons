package org.aksw.commons.index.util;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

public class MapSuppliers {


    public static <X> CMapSupplier<X> wrapAsCMap(MapSupplier supp, Supplier<X> initValueSupp) {
        return new CMapSupplier<X>() {
            @SuppressWarnings("unchecked")
            @Override
            public <K, V> CMap<K, V, X> get() {
                X data = initValueSupp.get();
                Map<K, V> core = (Map<K, V>)supp.get();
                return new CMapImpl<K, V, X>(core, data);
            }
        };
    }

    public static <T> MapSupplier forTreeMap(Comparator<T> cmp) {
        return new MapSupplierTreeMap<T>(cmp);
    }

    public static class MapSupplierTreeMap<T>
        implements MapSupplier
    {
        protected Comparator<T> cmp;

        public MapSupplierTreeMap(Comparator<T> cmp) {
            super();
            this.cmp = cmp;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public <K, V> Map<K, V> get() {
           return new TreeMap<K, V>((Comparator)cmp);
        }
    }
}
