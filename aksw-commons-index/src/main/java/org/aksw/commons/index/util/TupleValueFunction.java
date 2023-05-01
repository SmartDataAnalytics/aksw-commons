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

import org.aksw.commons.tuple.accessor.TupleAccessor;

/**
 * Map a tuple-like object to a value
 *
 * @author Claus Stadler 11/09/2020
 *
 */
public interface TupleValueFunction<ComponentType, ValueType> {
    <TupleLike> ValueType map(TupleLike tupleLike, TupleAccessor<? super TupleLike, ? extends ComponentType> tupleAccessor);


    /**
     * TupleValueFunction that returns the tuple itself as the value
     *
     * @param <T>
     * @param <C>
     * @param tupleLike
     * @param tupleAccessor
     * @return
     */
    public static <ComponentType, ValueType> TupleValueFunction<ComponentType, ValueType> newIdentity() {
        return new TupleValueFunction<ComponentType, ValueType>() {
            @Override
            public <TupleLike> ValueType map(TupleLike tupleLike, TupleAccessor<? super TupleLike, ? extends ComponentType> tupleAccessor) {
              return (ValueType)tupleLike;
            }
        };
    }

    public static <ComponentType, ValueType> TupleValueFunction<ComponentType, ValueType> newComponent(int idx) {
        return new TupleValueFunction<ComponentType, ValueType>() {
            @SuppressWarnings("unchecked")
            @Override
            public <TupleLike> ValueType map(TupleLike tupleLike, TupleAccessor<? super TupleLike, ? extends ComponentType> tupleAccessor) {
              ValueType result = (ValueType)tupleAccessor.get(tupleLike, idx);
              return result;
            }
        };
    }

//    public static <T, C> T identity(T tupleLike, TupleAccessorCore<? super T, ? extends C> tupleAccessor) {
//        return tupleLike;
//    }

    /**
     * TupleValueFunction-compatible method that returns the component at index 0
     *
     * @param <T>
     * @param <C>
     * @param tupleLike
     * @param tupleAccessor
     * @return
     */
    public static <T, C> C component0(T tupleLike, TupleAccessor<? super T, ? extends C> tupleAccessor) {
        C result = tupleAccessor.get(tupleLike, 0);
        return result;
    }

}
