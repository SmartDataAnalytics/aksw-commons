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

package org.aksw.commons.tuple.bridge;

import org.aksw.commons.tuple.accessor.TupleAccessor;

/**
 * A bridge between domain objects and tuple representation.
 * The bridge adds the capability to create domain objects from tuples.
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D> The domain type of the tuple like object
 * @param <C> The component type
 */
public interface TupleBridge<D, C>
    extends TupleAccessor<D, C>
{
    int getDimension();

    /**
     * Build a domain object from some tuple-like object with its corresponding accessor.
     */
    <T> D build(T obj, TupleAccessor<? super T, ? extends C> accessor);

    default D build(@SuppressWarnings("unchecked") C... components) {
        return build(components, (cs, i) -> cs[i]);
    }

    default void validateBuildArg(TupleBridge<?, ?> bridge) {
        int cl = bridge.getDimension();
        int r = getDimension();

        if (cl != r) {
            throw new IllegalArgumentException("components.length must equal rank but " + cl + " != " + r);
        }
    }

    default C[] toComponentArray(D domainObject) {
        int len = getDimension();
        @SuppressWarnings("unchecked")
        C[] result = (C[])new Object[len];

        for (int i = 0; i < len; ++i) {
            result[i] = get(domainObject, i);
        }

        return result;
    }
}
