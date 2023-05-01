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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.index.util.Alt3;
import org.aksw.commons.tuple.accessor.TupleAccessor;
import org.aksw.commons.tuple.bridge.TupleBridge;

/**
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <D>
 * @param <C>
 * @param <V1>
 * @param <V2>
 */
public class StorageNodeAlt3<D, C, V1, V2, V3>
    extends StorageNodeAltBase<D, C, Alt3<V1, V2, V3>>
    implements StorageNodeMutable<D, C, Alt3<V1, V2, V3>>
{
    // protected List<? extends Meta2NodeCompound<D, C, ?>> children;
    protected Alt3<
        ? extends StorageNodeMutable<D, C, V1>,
        ? extends StorageNodeMutable<D, C, V2>,
        ? extends StorageNodeMutable<D, C, V3>> children;

    public StorageNodeAlt3(
            TupleBridge<D, C> tupleAccessor,
            StorageNodeMutable<D, C, V1> child1,
            StorageNodeMutable<D, C, V2> child2,
            StorageNodeMutable<D, C, V3> child3
        ) {
        super(tupleAccessor);
        this.children = Alt3.create(child1, child2, child3);
    }

    @Override
    public List<? extends StorageNode<D, C, ?>> getChildren() {
        return Arrays.asList(children.getV1(), children.getV2(), children.getV3());
    }

    @Override
    public <T> Stream<?> streamEntries(Alt3<V1, V2, V3> childStores, T tupleLike,
            TupleAccessor<? super T, ? extends C> tupleAccessor) {

        StorageNodeMutable<D, C, ?> pickedChild = children.getV1();
        Object pickedChildStore = childStores.getV1();

        // Delegate always to the first entry - we would need external information to do better
        return pickedChild.streamEntriesRaw(pickedChildStore, tupleLike, tupleAccessor);
    }

    /**
     * Return of a list with fresh stores of all children
     *
     */
    @Override
    public Alt3<V1, V2, V3> newStore() {
        return Alt3.create(
                children.getV1().newStore(),
                children.getV2().newStore(),
                children.getV3().newStore()
                );
    }

    /**
     * Checks whether all child store entries in the list of alternatives are empty
     *
     * (Not to be confused with checking the list of alternatives itself for emptiness)
     */
    @Override
    public boolean isEmpty(Alt3<V1, V2, V3> childStores) {
        StorageNodeMutable<D, C, ?> pickedChild = children.getV1();
        Object pickedChildStore = childStores.getV1();

        boolean result = pickedChild.isEmptyRaw(pickedChildStore);
        return result;
    }

    @Override
    public boolean add(Alt3<V1, V2, V3> childStores, D tupleLike) {
        boolean result = false;
        result = result || children.getV1().add(childStores.getV1(), tupleLike);
        children.getV2().add(childStores.getV2(), tupleLike);
        children.getV3().add(childStores.getV3(), tupleLike);

        return result;
    }

    @Override
    public boolean remove(Alt3<V1, V2, V3> childStores, D tupleLike) {
        boolean result = children.getV1().remove(childStores.getV1(), tupleLike);
        children.getV2().remove(childStores.getV2(), tupleLike);
        children.getV3().remove(childStores.getV3(), tupleLike);

        return result;
    }

    @Override
    public Object chooseSubStore(Alt3<V1, V2, V3> store, int subStoreIdx) {
        Object result;
        switch(subStoreIdx) {
        case 0: result = store.getV1(); break;
        case 1: result = store.getV2(); break;
        case 2: result = store.getV3(); break;
        default: throw new IndexOutOfBoundsException("Index must be 0 or 1; was " + subStoreIdx);
        }
        return result;
    }

    @Override
    public void clear(Alt3<V1, V2, V3> store) {
        children.getV1().clear(store.getV1());
        children.getV2().clear(store.getV2());
        children.getV3().clear(store.getV3());
    }

//    @Override
//    public <T> Streamer<Entry<V1, V2>, ? extends Entry<?, ?>> streamerForKeyAndSubStores(
//            int altIdx,
//            T pattern,
//            TupleAccessorCore<? super T, ? extends C> accessor) {
//        return argStore -> Stream.of(Maps.immutableEntry(TupleFactory.create0(), chooseSubStore(argStore, altIdx)));
//    }


    @Override
    public String toString() {
        return "alt3(" + getChildren().stream().map(Object::toString).collect(Collectors.joining(" |\n")) + ")";
    }
}