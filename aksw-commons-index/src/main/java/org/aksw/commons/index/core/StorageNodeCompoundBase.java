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

import org.aksw.commons.tuple.bridge.TupleBridge;

/**
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D>
 * @param <C>
 * @param <V>
 */
public abstract class StorageNodeCompoundBase<D, C, V>
    extends StorageNodeBase<D, C, V>
    implements StorageNodeMutable<D, C, V>
{

    public StorageNodeCompoundBase(int[] tupleIdxs, TupleBridge<D, C> tupleAccessor) {
        super(tupleIdxs, tupleAccessor);
    }

//    public Meta2NodeCompoundBase(Meta2Node<D, C, V> child) {
//        super();
//        this.child = child;
//    }
//
//    @Override
//    public Meta2Node<D, C, V> getChild() {
//        return child;
//    }
}