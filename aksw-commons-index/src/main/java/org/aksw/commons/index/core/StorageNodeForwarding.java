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
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.commons.tuple.accessor.TupleAccessor;
import org.aksw.commons.tuple.bridge.TupleBridge;
import org.aksw.commons.util.stream.Streamer;

/**
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D>
 * @param <C>
 * @param <V>
 * @param <X>
 */
public abstract class StorageNodeForwarding<D, C, V, X extends StorageNode<D, C, V>>
    implements StorageNode<D, C, V>
{
    protected abstract X getDelegate();

    @Override
    public StorageNode<D, C, V> getPublicDelegate() {
        return getDelegate();
    }

    @Override
    public boolean isDelegate() {
        return true;
    }

    @Override
    public boolean isAltNode() {
        return getDelegate().isAltNode();
    }

    @Override
    public boolean isMapNode() {
        return getDelegate().isMapNode();
    }

    @Override
    public boolean isSetNode() {
        return getDelegate().isSetNode();
    }

    @Override
    public Map<?, ?> getStoreAsMap(Object store) {
        return getDelegate().getStoreAsMap(store);
    }

    @Override
    public Set<?> getStoreAsSet(Object store) {
        return getDelegate().getStoreAsSet(store);
    }

    @Override
    public List<? extends StorageNode<D, C, ?>> getChildren() {
        return getDelegate().getChildren();
    }

    @Override
    public int[] getKeyTupleIdxs() {
        return getDelegate().getKeyTupleIdxs();
    }

    @Override
    public TupleBridge<D, C> getTupleAccessor() {
        return getDelegate().getTupleAccessor();
    }

    @Override
    public <T> Streamer<V, C> streamerForKeysAsComponent(T pattern,
            TupleAccessor<? super T, ? extends C> accessor) {
        return getDelegate().streamerForKeysAsComponent(pattern, accessor);
    }

    @Override
    public <T> Streamer<V, List<C>> streamerForKeysAsTuples(T pattern,
            TupleAccessor<? super T, ? extends C> accessor) {
        return getDelegate().streamerForKeysAsTuples(pattern, accessor);
    }

    @Override
    public <T> Streamer<V, ?> streamerForKeys(T pattern, TupleAccessor<? super T, ? extends C> accessor) {
        return getDelegate().streamerForKeys(pattern, accessor);
    }

    @Override
    public C getKeyComponentRaw(Object key, int idx) {
        return getDelegate().getKeyComponentRaw(key, idx);
    }

    @Override
    public Object chooseSubStore(V store, int subStoreIdx) {
        return getDelegate().chooseSubStore(store, subStoreIdx);
    }

    @Override
    public <T> Streamer<V, ?> streamerForValues(T pattern, TupleAccessor<? super T, ? extends C> accessor) {
        return getDelegate().streamerForValues(pattern, accessor);
    }

    @Override
    public <T> Streamer<V, ? extends Entry<?, ?>> streamerForKeyAndSubStoreAlts(T pattern,
            TupleAccessor<? super T, ? extends C> accessor) {
        return getDelegate().streamerForKeyAndSubStoreAlts(pattern, accessor);
    }

    @Override
    public <T> Stream<?> streamEntries(V store, T tupleLike, TupleAccessor<? super T, ? extends C> tupleAccessor) {
        return getDelegate().streamEntries(store, tupleLike, tupleAccessor);
    }

}
