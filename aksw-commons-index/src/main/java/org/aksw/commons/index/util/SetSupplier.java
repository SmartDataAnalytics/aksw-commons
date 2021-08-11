/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.aksw.commons.index.util;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

/**
 * Helper interface for creating new set instances with automatically inferred
 * types
 *
 * @author Claus Stadler 11/09/2020
 *
 */
public interface SetSupplier {
    <V> Set<V> get();

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

    public static SetSupplier forTreeSet(Comparator<?> cmp) {
        return new SetSupplierTreeSet(cmp);
    }

    public static class SetSupplierTreeSet implements SetSupplier {
        protected Comparator<?> cmp;

        public SetSupplierTreeSet(Comparator<?> cmp) {
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
