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

import java.util.function.BiFunction;

/**
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D>
 * @param <C>
 * @param <V>
 * @param <X>
 */
public class StorageNodeDomainWrapping<D, C, V, X extends StorageNodeMutable<D, C, V>>
    extends StorageNodeMutableForwarding<D, C, V, X>
{
    protected X target;
    protected BiFunction<? super StorageNodeMutable<D, C, V>, ? super X, ? extends X> storeWrapper;

    public StorageNodeDomainWrapping(X target,
            BiFunction<? super StorageNodeMutable<D, C, V>, ? super X, ? extends X> storeWrapper) {
        super();
        this.target = target;
        this.storeWrapper = storeWrapper;
    }

    @Override
    protected X getDelegate() {
        return target;
    }

    @Override
    public V newStore() {
//        V store = target.newStore();
//        V result = storeWrapper.apply(this, store);
//        return result;
        return null;
    }
}
