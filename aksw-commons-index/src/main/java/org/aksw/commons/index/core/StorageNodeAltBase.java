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

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.commons.index.util.Streamer;
import org.aksw.commons.tuple.TupleAccessor;
import org.aksw.commons.tuple.TupleAccessorCore;

import com.google.common.collect.Maps;


/**
 * Base class for index nodes that do not index by a key - or rather:
 * index by a single key that is a zero-sized tuple
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D>
 * @param <C>
 * @param <V>
 */
public abstract class StorageNodeAltBase<D, C, V>
    extends StorageNodeBase<D, C, V>
{
    public StorageNodeAltBase(TupleAccessor<D, C> tupleAccessor) {
        super(new int[] {}, tupleAccessor);
    }

    @Override
    public boolean isAltNode() {
        return true;
    }

    @Override
    public <T> Streamer<V, C> streamerForKeysAsComponent(T pattern,
            TupleAccessorCore<? super T, ? extends C> accessor) {
        throw new UnsupportedOperationException("Cannot stream keys as components if there are no keys");
    }

    @Override
    public <T> Streamer<V, List<C>> streamerForKeysAsTuples(T pattern,
            TupleAccessorCore<? super T, ? extends C> accessor) {
        return argStore -> Stream.of(Collections.emptyList());
    }

    @Override
    public <T> Streamer<V, V> streamerForValues(T pattern,
            TupleAccessorCore<? super T, ? extends C> accessor) {
        return argStore -> Stream.of(argStore);
    }


    @Override
    public <T> Streamer<V, ?> streamerForKeys(T pattern,
            TupleAccessorCore<? super T, ? extends C> accessor) {
        return argStore -> Stream.of(Collections.emptyList());
    }

    /**
     * Stream a single entry of the store alts themselves
     * children can then pick an alternative based on their index
     *
     */
    @Override
    public <T> Streamer<V, ? extends Entry<?, ?>> streamerForKeyAndSubStoreAlts(
            T pattern,
            TupleAccessorCore<? super T, ? extends C> accessor) {

        return argStore -> Stream.of(Maps.immutableEntry(null, argStore));
    }

    @Override
    public C getKeyComponentRaw(Object key, int idx) {
        throw new RuntimeException("Key is an empty tuple - there are no key components");
    }
}
