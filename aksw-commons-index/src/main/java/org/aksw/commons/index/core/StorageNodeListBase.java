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

import java.util.List;

import org.aksw.commons.index.util.ListSupplier;
import org.aksw.commons.tuple.bridge.TupleBridge;

/**
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D>
 * @param <C>
 * @param <V>
 */
abstract class StorageNodeListBase<D, C, V>
    extends StorageNodeBase<D, C, List<V>>
    implements StorageNodeMutable<D, C, List<V>>
{
    protected ListSupplier listSupplier;

    public StorageNodeListBase(
            int[] tupleIdxs,
            TupleBridge<D, C> tupleAccessor,
            ListSupplier listSupplier
        ) {
        super(tupleIdxs, tupleAccessor);
        this.listSupplier = listSupplier;
    }

    @Override
    public List<V> newStore() {
        return listSupplier.get();
    }

    
    @Override
    public boolean isListNode() {
        return true;
    }

    @Override
    public List<?> getStoreAsList(Object store) {
        return (List<?>)store;
    }

    @Override
    public boolean isEmpty(List<V> store) {
        boolean result = store.isEmpty();
        return result;
    }
}