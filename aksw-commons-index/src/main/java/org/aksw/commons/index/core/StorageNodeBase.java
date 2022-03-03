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

import org.aksw.commons.tuple.TupleAccessor;


/**
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D>
 * @param <C>
 * @param <V>
 */
public abstract class StorageNodeBase<D, C, V>
    implements StorageNode<D, C, V>
{
    protected int[] tupleIdxs;
    protected TupleAccessor<D, C> tupleAccessor;

    public StorageNodeBase(int[] tupleIdxs, TupleAccessor<D, C> tupleAccessor) {
        super();
        this.tupleIdxs = tupleIdxs;
        this.tupleAccessor = tupleAccessor;
    }

    @Override
    public int[] getKeyTupleIdxs() {
        return tupleIdxs;
    }

    @Override
    public TupleAccessor<D, C> getTupleAccessor() {
        return tupleAccessor;
    }
}