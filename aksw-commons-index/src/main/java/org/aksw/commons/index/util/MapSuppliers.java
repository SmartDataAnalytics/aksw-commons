package org.aksw.commons.index.util;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class MapSuppliers {

    public static <T> MapSupplierStd forTreeMap(Comparator<T> cmp) {
        return new MapSupplierTreeMap<T>(cmp);
    }

    public static class MapSupplierTreeMap<T>
        implements MapSupplierStd
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
