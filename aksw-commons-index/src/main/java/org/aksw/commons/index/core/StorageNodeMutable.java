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

package org.aksw.commons.index.core;

/**
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D>
 * @param <C>
 * @param <V>
 */
public interface StorageNodeMutable<D, C, V>
    extends StorageNode<D, C, V>
{
    V newStore();

    boolean isEmpty(V store);
    boolean add(V store, D tupleLike);
    boolean remove(V store, D tupleLike);

    /**
     * Clear a store's content. Cascades to any sub-stores.
     */
    void clear(V store);

    @SuppressWarnings("unchecked")
    default boolean isEmptyRaw(Object store) {
        return isEmpty((V)store);
    }

    @SuppressWarnings("unchecked")
    default boolean addRaw(Object store, D tupleLike) {
        return add((V)store, tupleLike);
    }

    @SuppressWarnings("unchecked")
    default boolean removeRaw(Object store, D tupleLike) {
        return remove((V)store, tupleLike);
    }

    @SuppressWarnings("unchecked")
    default void clearRaw(Object store) {
        clear((V)store);
    }

}
