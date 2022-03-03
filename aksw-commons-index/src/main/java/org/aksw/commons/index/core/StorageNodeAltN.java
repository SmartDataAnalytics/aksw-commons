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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.tuple.TupleAccessor;
import org.aksw.commons.tuple.TupleAccessorCore;


/**
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D>
 * @param <C>
 */
public class StorageNodeAltN<D, C>
    extends StorageNodeAltBase<D, C, Object[]>
    implements StorageNodeMutable<D, C, Object[]>
{
    protected List<? extends StorageNodeMutable<D, C, ?>> children;

    public StorageNodeAltN(
            TupleAccessor<D, C> tupleAccessor,
            List<? extends StorageNodeMutable<D, C, ?>> children
            ) {
        super(tupleAccessor);
        this.children = children;
    }

    @Override
    public List<? extends StorageNode<D, C, ?>> getChildren() {
        return children;
    }

    @Override
    public <T> Stream<?> streamEntries(Object[] childStores, T tupleLike,
            TupleAccessorCore<? super T, ? extends C> tupleAccessor) {

        StorageNodeMutable<D, C, ?> pickedChild = children.get(0);
        Object pickedChildStore = childStores[0];

        // Delegate always to the first entry - we would need external information to do better
        return pickedChild.streamEntriesRaw(pickedChildStore, tupleLike, tupleAccessor);
    }

    /**
     * Return of a list with fresh stores of all children
     *
     */
    @Override
    public Object[] newStore() {
        Object[] result = new Object[children.size()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = children.get(i).newStore();
        }
        return result;
    }

    /**
     * Checks whether all child store entries in the list of alternatives are empty
     *
     * (Not to be confused with checking the list of alternatives itself for emptiness)
     */
    @Override
    public boolean isEmpty(Object[] childStores) {
        StorageNodeMutable<D, C, ?> pickedChild = children.get(0);
        Object pickedChildStore = childStores[0];

        boolean result = pickedChild.isEmptyRaw(pickedChildStore);
        return result;
    }

    @Override
    public boolean add(Object[] childStores, D tupleLike) {
        boolean result = false;
        for (int i = 0; i < children.size(); ++i) {
            StorageNodeMutable<D, C, ?> child = children.get(i);
            Object childStore = childStores[i];

            result = child.addRaw(childStore, tupleLike);
        }

        return result;
    }

    @Override
    public boolean remove(Object[] childStores, D tupleLike) {
        boolean result = false;
        for (int i = 0; i < children.size(); ++i) {
            StorageNodeMutable<D, C, ?> child = children.get(i);
            Object childStore = childStores[i];

            result = child.removeRaw(childStore, tupleLike);
        }

        return result;
    }

    @Override
    public void clear(Object[] childStores) {
        for (int i = 0; i < children.size(); ++i) {
            StorageNodeMutable<D, C, ?> child = children.get(i);
            Object childStore = childStores[i];

            child.clearRaw(childStore);
        }
    }


    @Override
    public String toString() {
        return "altN(" + children.stream().map(Object::toString).collect(Collectors.joining(" |\n")) + ")";
    }


//    @Override
//    public <T> Streamer<Object[], ? extends Entry<?, ?>> streamerForKeyAndSubStores(
//            int altIdx,
//            T pattern,
//            TupleAccessorCore<? super T, ? extends C> accessor) {
//        return argStore -> Stream.of(Maps.immutableEntry(TupleFactory.create0(), chooseSubStore(argStore, altIdx)));
//    }

    @Override
    public Object chooseSubStore(Object[] store, int subStoreIdx) {
        return store[subStoreIdx];
    }
}
