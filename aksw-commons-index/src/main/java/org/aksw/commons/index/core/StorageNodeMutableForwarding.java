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
 * @param <X>
 */
public abstract class StorageNodeMutableForwarding<D, C, V, X extends StorageNodeMutable<D, C, V>>
    extends StorageNodeForwarding<D, C, V, X>
    implements StorageNodeMutable<D, C, V>
{
    @Override
    public V newStore() {
        return getDelegate().newStore();
    }

    @Override
    public boolean isEmpty(V store) {
        return getDelegate().isEmpty(store);
    }

    @Override
    public boolean add(V store, D tupleLike) {
        return getDelegate().add(store, tupleLike);
    }

    @Override
    public boolean remove(V store, D tupleLike) {
        return getDelegate().remove(store, tupleLike);
    }

    @Override
    public void clear(V store) {
        getDelegate().clear(store);
    }

}
