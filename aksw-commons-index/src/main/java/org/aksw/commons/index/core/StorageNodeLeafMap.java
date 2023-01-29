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
import java.util.stream.Stream;

import org.aksw.commons.index.util.MapSupplier;
import org.aksw.commons.index.util.TupleValueFunction;
import org.aksw.commons.tuple.accessor.TupleAccessor;
import org.aksw.commons.tuple.bridge.TupleBridge;

/**
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D>
 * @param <C>
 * @param <K>
 * @param <V>
 */
public class StorageNodeLeafMap<D, C, K, V, M extends Map<K, V>>
    extends StorageNodeMapBase<D, C, K, V, M>
{
    protected TupleValueFunction<C, V> valueFunction;

    public StorageNodeLeafMap(
            int[] tupleIdxs,
            TupleBridge<D, C> tupleAccessor,
            MapSupplier mapSupplier,
            TupleValueFunction<C, K> keyFunction,
            TupleAccessor<? super K, ? extends C> keyToComponent,
            TupleValueFunction<C, V> valueFunction
            ) {
        super(tupleIdxs, tupleAccessor, mapSupplier, keyFunction, keyToComponent);
        this.valueFunction = valueFunction;
    }

    @Override
    public List<StorageNode<D, C, ?>> getChildren() {
        // TODO We need to declare a 'fake' child - so that this node is an index for the child
        // the child a store conceptually contains zero tuples but indexed by the keys of this node

        // return Collections.singletonList(StorageComposers.leafSet(tupleAccessor, LinkedHashSet::new));
        return Collections.emptyList();
    }

    // @Override
//    public K tupleToKey(D tupleLike, TupleAccessor<? super D, ? extends C> tupleAccessor) {
//        K result = keyFunction.createKey(tupleLike, tupleAccessor);
//        return result;
//    }

    @Override
    public boolean add(M map, D tupleLike) {
        K key = tupleToKey(tupleLike);
        V newValue = valueFunction.map(tupleLike, tupleAccessor);

        if(map.containsKey(key)) {
            V oldValue = map.get(key);
            if (!newValue.equals(oldValue)) {
                throw new RuntimeException("Insert [" + newValue + "] failed for key " + key + " because it already maps to " + oldValue);
            }
        } else {
            map.put(key, newValue);
        }

        return true;
    }

    @Override
    public boolean remove(M map, D tupleLike) {
        K key = tupleToKey(tupleLike);
        boolean result = map.containsKey(key);
        if (result) {
            map.remove(key);
        }

        return result;
    }

    @Override
    public void clear(M store) {
        store.clear();
    }


    @Override
    public String toString() {
        return "leafMap(" + Arrays.toString(tupleIdxs) + ")";
    }

    @Override
    public <T> Stream<Entry<K, ?>> streamEntries(M map, T tupleLike, TupleAccessor<? super T, ? extends C> tupleAccessor) {
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

            V value = map.get(key);
            childStream = value == null
                    ? Stream.empty()
                    : Stream.of(new SimpleEntry<>(key, value));
        } else {
            childStream = map.entrySet().stream().map(e -> new SimpleEntry<>(e.getKey(), e.getValue()));
        }

        return childStream;
    }
}