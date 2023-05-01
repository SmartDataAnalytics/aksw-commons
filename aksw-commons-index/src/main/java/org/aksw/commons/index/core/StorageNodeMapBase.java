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
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.commons.index.util.MapSupplier;
import org.aksw.commons.index.util.TupleValueFunction;
import org.aksw.commons.tuple.accessor.TupleAccessor;
import org.aksw.commons.tuple.bridge.TupleBridge;
import org.aksw.commons.util.stream.Streamer;

import com.google.common.collect.Maps;

/**
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D>
 * @param <C>
 * @param <K>
 * @param <V>
 */
abstract class StorageNodeMapBase<D, C, K, V, M extends Map<K, V>>
    extends StorageNodeBase<D, C, M>
    implements StorageNodeMutable<D, C, M>
{
    protected MapSupplier mapSupplier;
    // protected TupleToKey<? extends K, C> keyFunction;
    protected TupleValueFunction<C, K> keyFunction;

    // Reverse mapping of key to components
    protected TupleAccessor<? super K, ? extends C> keyToComponent;

    public K tupleToKey(D tupleLike) {
        K result = keyFunction.map(tupleLike, (d, i) -> tupleAccessor.get(d, tupleIdxs[i]));
        return result;
    }

//
//    @SuppressWarnings("unchecked")
//    public Map<K, V> asMap(Object store) {
//        return (Map<K, V>)store;
//    }


    public StorageNodeMapBase(
            int[] tupleIdxs,
            TupleBridge<D, C> tupleAccessor,
            MapSupplier mapSupplier,
            //TupleToKey<? extends K, C> keyFunction
            //Function<? super D, ? extends K> keyFunction
            TupleValueFunction<C, K> keyFunction,
            TupleAccessor<? super K, ? extends C> keyToComponent
            ) {
        super(tupleIdxs, tupleAccessor);
        this.mapSupplier = mapSupplier;
        this.keyFunction = keyFunction;
        this.keyToComponent = keyToComponent;
    }

    @Override
    public boolean isMapNode() {
        return true;
    }

    @Override
    public M getStoreAsMap(Object store) {
        return (M)store;
    }

    @Override
    public M newStore() {
        return (M)mapSupplier.get();
    }


    @Override
    public boolean isEmpty(M map) {
        boolean result = map.isEmpty();
        return result;
    }


    public static <T, C> Object[] projectTupleToArray(int[] tupleIdxs, T tupleLike, TupleAccessor<T, C> tupleAccessor) {
        Object[] result = new Object[tupleIdxs.length];
        for (int i = 0; i < tupleIdxs.length; ++i) {
            C componentValue = tupleAccessor.get(tupleLike, tupleIdxs[i]);
            if (componentValue == null) {
                result = null;
                break;
            }
            result[i] = componentValue;
        }

        return result;
    }


    public <T> Streamer<M, K> streamerForKeysUnderConstraints(
            T tupleLike,
            TupleAccessor<? super T, ? extends C> tupleAccessor)
    {
        Streamer<M, K> result;

        Object[] keyComponents = projectTupleToArray(tupleIdxs, tupleLike, tupleAccessor);
        if (keyComponents != null) {
            @SuppressWarnings("unchecked")
            K key = keyFunction.map(keyComponents, (x, i) -> (C)x[i]);

            result = argMap -> argMap.containsKey(key)
                    ? Stream.of(key)
                    : Stream.empty();
        } else {
            result = argMap -> argMap.keySet().stream();
        }

        return result;
    }


    public <T> Streamer<M, V> streamerForValuesUnderConstraints(
            T tupleLike,
            TupleAccessor<? super T, ? extends C> tupleAccessor)
    {
        Streamer<M, V> result;

        Object[] keyComponents = projectTupleToArray(tupleIdxs, tupleLike, tupleAccessor);
        if (keyComponents != null) {
            K key = keyFunction.map(keyComponents, (x, i) -> (C)x[i]);

            result = argMap -> argMap.containsKey(key)
                    ? Stream.of(argMap.get(key))
                    : Stream.empty();
        } else {
            result = argMap -> argMap.values().stream();
        }

        return result;
    }



    public <T> Streamer<M, Entry<K, V>> streamerForEntriesUnderConstraints(
            T tupleLike,
            TupleAccessor<? super T, ? extends C> tupleAccessor)
    {
        Object[] tmp = new Object[tupleIdxs.length];
        boolean eligibleAsKey = true;
        for (int i = 0; i < tupleIdxs.length; ++i) {
            C componentValue = tupleAccessor.get(tupleLike, tupleIdxs[i]);
            if (componentValue == null) {
                eligibleAsKey = false;
                break;
            }
            tmp[i] = componentValue;
        }

        Streamer<M, Entry<K, V>> result;

        if (eligibleAsKey) {
            K key = keyFunction.map(tmp, (x, i) -> (C)x[i]);

            result = argMap -> argMap.containsKey(key)
                    ? Stream.of(Maps.immutableEntry(key, argMap.get(key)))
                    : Stream.empty();
        } else {
            result = argMap -> argMap.entrySet().stream();
        }

        return result;
    }


    @Override
    public <T> Streamer<M, C> streamerForKeysAsComponent(
            T pattern,
            TupleAccessor<? super T, ? extends C> accessor) {

        Streamer<M, K> baseStreamer = streamerForKeysUnderConstraints(pattern, accessor);
        // FIXME Ensure that the keys can be cast as components!
        return argMap -> baseStreamer.stream(argMap).map(key -> (C)key);
    }


    @Override
    public <T> Streamer<M, V> streamerForValues(T pattern, TupleAccessor<? super T, ? extends C> accessor) {
        return streamerForValuesUnderConstraints(pattern, accessor);
//        Streamer<Map<K, V>, K> baseStreamer = streamerForKeysUnderConstraints(pattern, accessor);
//
//        // The base streamer ensures that the key exists in the map
//        return argMap -> baseStreamer.stream(argMap).map(key -> argMap.get(key));
    }


//    @Override
//    public Streamer<Map<K, V>, V> streamerForValues() {
//        return argMap -> argMap.values().stream();
//    }


    @Override
    public <T> Streamer<M, List<C>> streamerForKeysAsTuples(
            T pattern,
            TupleAccessor<? super T, ? extends C> accessor) {
        return null;
    }

    @Override
    public <T> Streamer<M, K> streamerForKeys(T pattern, TupleAccessor<? super T, ? extends C> accessor) {
        return streamerForKeysUnderConstraints(pattern, accessor);
    }


    @Override
    public <T> Streamer<M, Entry<K, V>> streamerForKeyAndSubStoreAlts(
            T pattern,
            TupleAccessor<? super T, ? extends C> accessor) {
        // TODO Assert that altIdx == 0
        return streamerForEntriesUnderConstraints(pattern, accessor);
    }

    @Override
    public C getKeyComponentRaw(Object key, int idx) {
        C result = keyToComponent.get((K)key, idx);
        return result;
    }

    public C getKeyComponent(K key, int idx) {
        C result = keyToComponent.get(key, idx);
        return result;
    }


    @Override
    public Object chooseSubStore(M store, int subStoreIdx) {
        if (subStoreIdx != 0) {
            throw new IndexOutOfBoundsException("Index must be 0 for inner maps");
        }

        // Return the store itself
        return store;
    }
}