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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.commons.index.util.SetSupplier;
import org.aksw.commons.index.util.TupleValueFunction;
import org.aksw.commons.tuple.accessor.TupleAccessor;
import org.aksw.commons.tuple.bridge.TupleBridge;
import org.aksw.commons.util.stream.Streamer;

import com.google.common.collect.Maps;

/**
 * Essentially a view of a Set<K> as a Map<K, Void>
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D>
 * @param <C>
 * @param <V>
 */
public class StorageNodeLeafComponentSet<D, C, V, S extends Set<V>>
    extends StorageNodeSetBase<D, C, V, S>
{
    protected TupleValueFunction<C, V> valueFunction;

    // Reverse mapping of key to components
    protected TupleAccessor<? super V, ? extends C> keyToComponent;

//    public StorageNodeLeafComponentSet(
//            TupleAccessor<D, C> tupleAccessor,
//            SetSupplier setSupplier,
//            boolean holdsDomainTuples,
//            TupleValueFunction<C, V> valueFunction,
//            TupleAccessorCore<? super V, ? extends C> keyToComponent
//            ) {
//        super(new int[] {}, tupleAccessor, setSupplier);
//        this.valueFunction = valueFunction;
//        this.keyToComponent = keyToComponent;
//    }

    public StorageNodeLeafComponentSet(
            int tupleIdxs[],
            TupleBridge<D, C> tupleAccessor,
            SetSupplier setSupplier,
            TupleValueFunction<C, V> valueFunction,
            TupleAccessor<? super V, ? extends C> keyToComponent
            ) {
        super(tupleIdxs, tupleAccessor, setSupplier);
        this.valueFunction = valueFunction;
        this.keyToComponent = keyToComponent;
    }

    public V tupleToValue(D tupleLike) {
        V result = valueFunction.map(tupleLike, (d, i) -> tupleAccessor.get(d, tupleIdxs[i]));
        return result;
    }


    @Override
    public List<StorageNode<D, C, ?>> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public boolean add(S set, D tupleLike) {
        V newValue = tupleToValue(tupleLike);

        // TODO We should use a separate type that explicitly allows null placeholders
        boolean result = set != null
                ? set.add(newValue)
                : false;

        return result;
    }

    @Override
    public boolean remove(S set, D tupleLike) {
        V newValue = tupleToValue(tupleLike);
        boolean result = set.remove(newValue);
        return result;
    }

    @Override
    public void clear(S store) {
        store.clear();
    }

    @Override
    public String toString() {
        return "(" + Arrays.toString(tupleIdxs) + ")";
    }

    @Override
    public <T> Streamer<S, C> streamerForKeysAsComponent(T pattern,
            TupleAccessor<? super T, ? extends C> accessor) {
//      return argSet -> argSet.stream();

        Streamer<S, V> baseStreamer = streamerForKeysUnderConstraints(pattern, accessor);
        // FIXME Ensure that the keys can be cast as components!
        return argSet -> baseStreamer.stream(argSet).map(key -> (C)key);
    }


    public <T> Streamer<S, V> streamerForKeysUnderConstraints(
            T tupleLike,
            TupleAccessor<? super T, ? extends C> tupleAccessor)
    {
        Streamer<S, V> result;

        Object[] keyComponents = StorageNodeMapBase.projectTupleToArray(tupleIdxs, tupleLike, tupleAccessor);
        if (keyComponents != null) {
            @SuppressWarnings("unchecked")
            V key = valueFunction.map(keyComponents, (x, i) -> (C)x[i]);

            result = argSet -> argSet.contains(key)
                    ? Stream.of(key)
                    : Stream.empty();
        } else {
            result = argSet -> argSet.stream();
        }

        return result;
    }


    @Override
    public <T> Streamer<S, List<C>> streamerForKeysAsTuples(T pattern,
            TupleAccessor<? super T, ? extends C> accessor) {
        return null;
    }

    @Override
    public <T> Streamer<S, V> streamerForValues(T pattern, TupleAccessor<? super T, ? extends C> accessor) {
        throw new UnsupportedOperationException("There are no values to stream (Values can be seen as Tuple0 though)");
    }


    @Override
    public <T> Streamer<S, ? extends Entry<?, ?>> streamerForKeyAndSubStoreAlts(
//            int altIdx,
            T pattern,
            TupleAccessor<? super T, ? extends C> accessor) {
        Streamer<S, Entry<?, ?>> result = streamerForKeysUnderConstraints(pattern, accessor)
                .mapItems(v -> Maps.immutableEntry(v, null));

        return result;
    }

    @Override
    public <T> Stream<V> streamEntries(S set, T tupleLike, TupleAccessor<? super T, ? extends C> tupleAccessor) {
        throw new UnsupportedOperationException("There are no entries to stream (Values can be seen as Tuple0 though)");
    }

    @Override
    public <T> Streamer<S, ?> streamerForKeys(T pattern, TupleAccessor<? super T, ? extends C> accessor) {
        return streamerForKeysUnderConstraints(pattern, accessor);
    }

    @Override
    public C getKeyComponentRaw(Object key, int idx) {
        C result = keyToComponent.get((V)key, idx);
        return result;
    }

//    @Override
//    public Object chooseSubStore(Set<V> store, int subStoreIdx) {
//        throw new UnsupportedOperationException("leaf sets do not have a sub store");
//    }

    @Override
    public Object chooseSubStore(S store, int subStoreIdx) {
        if (subStoreIdx != 0) {
            throw new IndexOutOfBoundsException("Index must be 0 for inner maps");
        }

        // Return the store itself
        return store;
    }

}