package org.aksw.commons.index.util;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

public class SetSuppliers {

    public static <X> CSetSupplier<X> wrapAsCSet(SetSupplier setSupplier, Supplier<X> valueSupplier) {
        return new CSetSupplier<X>() {
            @Override
            public <T> CSet<T, X> get() {
                Set<T> tmp = setSupplier.<T>get();
                return new CSetImpl<>(tmp, valueSupplier.get());
            }
        };
    }

    /**
     * A supplier that supplies null instead of set instances. In nested structures
     * such null values may act as placeholders that are replaced in a
     * post-processing step.
     *
     * @return 'null' casted to the appropriate type.
     */
    public static SetSupplier none() {
        return new SetSupplier() {
            @Override
            public <V> Set<V> get() {
                return (Set<V>) null;
            }
        };
    }

    /**
     * Force cast to the requested type. Useful for e.g. TreeSets: While e.g.
     * HashSet::new can supply set instances for any generic type, TreeSets are
     * dependent on a comparator which may only work with specific types.
     *
     * @param setSupplier
     * @return
     */
    public static SetSupplier forceCast(Supplier<Set<?>> setSupplier) {
        return new SetSupplier() {
            @SuppressWarnings("unchecked")
            @Override
            public <V> Set<V> get() {
                return (Set<V>) setSupplier.get();
            }
        };
    }

    public static <X> SetSupplier forTreeSet(Comparator<X> cmp) {
        return new SetSupplierTreeSet<X>(cmp);
    }

    public static class SetSupplierTreeSet<X> implements SetSupplier {
        protected Comparator<X> cmp;

        public SetSupplierTreeSet(Comparator<X> cmp) {
            super();
            this.cmp = cmp;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public <T> Set<T> get() {
            return new TreeSet<T>((Comparator) cmp);
        }
    }
}
