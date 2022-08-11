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
import org.aksw.commons.tuple.TupleAccessor;
import org.aksw.commons.tuple.TupleAccessorCore;
import org.aksw.commons.util.stream.Streamer;

import com.google.common.collect.Maps;

/**
 * Essentially a view of a List&lt;D&gt; as a {@code Map<EMPTY_LIST, List<D>>}
 * where there for a non-empty List&lt;D&gt; there is exactly a single key that is the empty list.
 *
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D>
 * @param <C>
 * @param <V>
 */
public class StorageNodeLeafDomainSet<D, C, V, S extends Set<V>>
    extends StorageNodeSetBase<D, C, V, S>
{
    protected TupleValueFunction<C, V> valueFunction;

    public StorageNodeLeafDomainSet(
            TupleAccessor<D, C> tupleAccessor,
            SetSupplier setSupplier,
            TupleValueFunction<C, V> valueFunction
            ) {
        super(new int[] {}, tupleAccessor, setSupplier);
        this.valueFunction = valueFunction;
    }

//    public StorageNodeLeafDomainSet(
//            int tupleIdxs[],
//            TupleAccessor<D, C> tupleAccessor,
//            SetSupplier setSupplier,
//            boolean holdsDomainTuples,
//            TupleValueFunction<C, V> valueFunction
//            ) {
//        super(tupleIdxs, tupleAccessor, setSupplier);
//        this.valueFunction = valueFunction;
//    }
//

    @Override
    public boolean holdsDomainTuples() {
        return true;
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
            TupleAccessorCore<? super T, ? extends C> accessor) {
        throw new UnsupportedOperationException("Cannot stream keys as components if there are no keys");
    }

    @Override
    public <T> Streamer<S, List<C>> streamerForKeysAsTuples(T pattern,
            TupleAccessorCore<? super T, ? extends C> accessor) {
        return argStore -> Stream.of(Collections.emptyList());
    }

    @Override
    public <T> Streamer<S, V> streamerForValues(T pattern, TupleAccessorCore<? super T, ? extends C> accessor) {
        return argSet -> argSet.stream();
    }

//    @Override
//    public Streamer<Set<V>, V> streamerForValues() {
//        return argSet -> argSet.stream();
//    }


    @Override
    public <T> Streamer<S, ? extends Entry<?, ?>> streamerForKeyAndSubStoreAlts(
//            int altIdx,
            T pattern,
            TupleAccessorCore<? super T, ? extends C> accessor) {
        return argSet -> Stream.of(Maps.immutableEntry(Collections.emptyList(), argSet));
//        throw new UnsupportedOperationException("leaf sets do not have a sub store");
//    	return argSet -> argSet.stream().map(item -> Entry<>);
    }

    @Override
    public <T> Stream<V> streamEntries(S set, T tupleLike, TupleAccessorCore<? super T, ? extends C> tupleAccessor) {
        // FIXME We need to filter the result stream by the components of the tuple like!
        return set.stream();
    }

    @Override
    public <T> Streamer<S, ?> streamerForKeys(T pattern, TupleAccessorCore<? super T, ? extends C> accessor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public C getKeyComponentRaw(Object key, int idx) {
        throw new RuntimeException("Key is an empty tuple - there are no key components");
    }

//    @Override
//    public Object chooseSubStore(Set<V> store, int subStoreIdx) {
//        throw new UnsupportedOperationException("leaf sets do not have a sub store");
//    }

    @Override
    public Object chooseSubStore(S store, int subStoreIdx) {
        if (subStoreIdx != 0) {
            throw new IndexOutOfBoundsException("Index must be 0 for leaf maps");
        }

        // Return the store itself
        return store;
    }

}