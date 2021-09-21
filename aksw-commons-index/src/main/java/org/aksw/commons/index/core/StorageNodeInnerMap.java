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

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Stream;

import org.aksw.commons.index.util.MapSupplier;
import org.aksw.commons.index.util.TupleValueFunction;
import org.aksw.commons.tuple.TupleAccessor;
import org.aksw.commons.tuple.TupleAccessorCore;

/**
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D> The domain type of tuple
 * @param <C> The component type of the domain tuple
 * @param <K> The key type of the map; depends on one or more components
 * @param <V> The value type of the map which is
 */
public class StorageNodeInnerMap<D, C, K, V, M extends Map<K, V>>
    extends StorageNodeMapBase<D, C, K, V, M>
{
    protected StorageNodeMutable<D, C, V> child;

    public StorageNodeInnerMap(
            int[] tupleIdxs,
            TupleAccessor<D, C> tupleAccessor,
            StorageNodeMutable<D, C, V> child,
            MapSupplier<M> mapSupplier,
            TupleValueFunction<C, K> keyFunction,
            TupleAccessorCore<? super K, ? extends C> keyToComponent) {
        super(tupleIdxs, tupleAccessor, mapSupplier, keyFunction, keyToComponent);
        this.child = child;
    }


    @Override
    public List<StorageNode<D, C, ?>> getChildren() {
        return Collections.singletonList(child);
    }

    @Override
    public M newStore() {
        return mapSupplier.get();
    }

    // @Override

    @Override
    public boolean add(M map, D tupleLike) {
        K key = tupleToKey(tupleLike);

        V v = map.get(key);
        if (v == null) {
            // TODO If we need to create a new child store then the result of this function
            // must be true - validate that child.add also returns true
            v = child.newStore();
            map.put(key, v);
        }

        boolean result = child.add(v, tupleLike);
        return result;
    }

    @Override
    public boolean remove(M map, D tupleLike) {
        K key = tupleToKey(tupleLike);

        boolean result = false;
        V v = map.get(key);
        if (v != null) {
            result = child.remove(v, tupleLike);
            if (child.isEmpty(v)) {
                map.remove(key);
            }
        }

        return result;
    }

    @Override
    public void clear(M store) {
        for (V subStoreAlts : store.values()) {
            child.clear(subStoreAlts);
        }

        store.clear();
    }


    @Override
    public String toString() {
        return "innerMap(" + Arrays.toString(tupleIdxs) + " -> " + Objects.toString(child) + ")";
    }



    @Override
    public <T> Stream<Entry<K, ?>> streamEntries(M map, T tupleLike, TupleAccessorCore<? super T, ? extends C> tupleAccessor) {

        // Check whether the components of the given tuple are all non-null such that we can
        // create a key from them
        Object[] tmp = new Object[tupleIdxs.length];
        boolean eligibleAsKey = true;
        for (int i = 0; i < tupleIdxs.length; ++i) {
            C componentValue = tupleAccessor.get(tupleLike, i);
            if (componentValue == null) {
                eligibleAsKey = false;
                break;
            }
            tmp[i] = componentValue;
        }

        Stream<Entry<K, ?>> childStream;

        // If we have a key we can do a lookup in the map
        // otherwise we have to scan all keys
        if (eligibleAsKey) {
            K key = keyFunction.map(tmp, (x, i) -> (C)x[i]);

            V childStore = map.get(key);
            childStream = childStore == null
                    ? Stream.empty()
                    : child.streamEntries(childStore, tupleLike, tupleAccessor).map(v -> new SimpleEntry<>(key, v));
        } else {
            childStream = map.entrySet().stream().flatMap(
                    e -> child.streamEntries(e.getValue(), tupleLike, tupleAccessor).map(v -> new SimpleEntry<>(e.getKey(), v)));
        }

        return childStream;
    }

}

