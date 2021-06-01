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

package org.aksw.commons.index;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.aksw.commons.index.core.StorageNodeAlt2;
import org.aksw.commons.index.core.StorageNodeAlt3;
import org.aksw.commons.index.core.StorageNodeAltN;
import org.aksw.commons.index.core.StorageNodeDictionary;
import org.aksw.commons.index.core.StorageNodeInnerMap;
import org.aksw.commons.index.core.StorageNodeLeafComponentSet;
import org.aksw.commons.index.core.StorageNodeLeafDomainList;
import org.aksw.commons.index.core.StorageNodeLeafDomainSet;
import org.aksw.commons.index.core.StorageNodeLeafMap;
import org.aksw.commons.index.core.StorageNodeMutable;
import org.aksw.commons.index.core.StorageNodeMutableForwardingBase;
import org.aksw.commons.index.core.StorageNodeWrapperCodec;
import org.aksw.commons.index.util.Alt2;
import org.aksw.commons.index.util.Alt3;
import org.aksw.commons.index.util.ListSupplier;
import org.aksw.commons.index.util.MapSupplier;
import org.aksw.commons.index.util.SetSupplier;
import org.aksw.commons.index.util.TupleValueFunction;
import org.aksw.commons.tuple.TupleAccessor;

import com.google.common.collect.HashBiMap;

/**
 * A collection of static methods for composing storage structures such
 * as arbitrarily nested maps
 *
 * @author Claus Stadler 11/09/2020
 *
 */
public class StorageComposers {

    public static <D, C> StorageNodeMutable<D, C, Set<D>> leafSet(
            SetSupplier setSupplier,
            TupleAccessor<D, C> tupleAccessor) {
        return new StorageNodeLeafDomainSet<D, C, D>(
                tupleAccessor,
                setSupplier,
                // Ugly identity mapping of domain tuples to themselves as values - can we do better?
                TupleValueFunction.newIdentity()
                );
    }

    public static <D, C> StorageNodeMutable<D, C, List<D>> leafList(
            ListSupplier listSupplier,
            TupleAccessor<D, C> tupleAccessor) {
        return new StorageNodeLeafDomainList<D, C, D>(
                tupleAccessor,
                listSupplier,
                // Ugly identity mapping of domain tuples to themselves as values - can we do better?
                TupleValueFunction.newIdentity()
                );
    }

    public static <D, C> StorageNodeMutable<D, C, Set<C>> leafComponentSet(
            int tupleIdx,
            SetSupplier setSupplier,
            TupleAccessor<D, C> tupleAccessor) {
        return new StorageNodeLeafComponentSet<D, C, C>(
                new int[] {tupleIdx},
                tupleAccessor,
                setSupplier,
                // TupleValueFunction that returns the tuple's component based on the first indexe in the tupleIdx array;
                // i.e. tuple[tupleIdx[0]]
                TupleValueFunction::component0,
                (key, idx) -> key // TODO Ensure that only component 0 is requested
                );
    }

    public static <D, C> StorageNodeMutable<D, C, Map<C, D>> leafMap(
            int tupleIdx,
            MapSupplier mapSupplier,
            TupleAccessor<D, C> tupleAccessor) {
        return new StorageNodeLeafMap<D, C, C, D>(
                new int[] {tupleIdx},
                tupleAccessor,
                mapSupplier,
                TupleValueFunction::component0,
                (key, idx) -> key, // TODO Ensure that only component 0 is requested
                // Ugly identity mapping of domain tuples to themselves as values - can we do better?
                TupleValueFunction.newIdentity()
                );
    }


    public static <D, C, V> StorageNodeMutable<D, C, Map<C, V>> innerMap(
            int tupleIdx,
            MapSupplier mapSupplier,
            StorageNodeMutable<D, C, V> child
            ) {

        TupleAccessor<D, C> tupleAccessor = child.getTupleAccessor();

        return new StorageNodeInnerMap<D, C, C, V>(
                new int[] {tupleIdx},
                tupleAccessor,
                child,
                mapSupplier,
                TupleValueFunction::component0,
                (key, idx) -> key // TODO Ensure that only component 0 is requested
                );
                // Return the element at index 0 of any tuple like object
                // (tupleLike, tupleAccessor) -> tupleAccessor.get(tupleLike, 0));
    }


    /**
     * Generic construction for composition from multiple composers
     * Breaks strong typing in contrast to static alternatives{1, 2, ...} constructions that
     * could e.g. yield Map<Foo, Alternatives3<Bar, Baz, Bax>> types
     *
     * @param <D>
     * @param <C>
     * @param <V>
     * @param tupleIdx
     * @param mapSupplier
     * @param child
     * @return
     */
    public static <D, C> StorageNodeMutable<D, C, ?> altN(
            List<? extends StorageNodeMutable<D, C, ?>> children
            ) {

        if (children.isEmpty()) {
            throw new IllegalArgumentException("At least one alternative must be provided");
        }

        // TODO Validate that all children use the same tuple acessor
        TupleAccessor<D, C> tupleAccessor = children.get(0).getTupleAccessor();
        return new StorageNodeAltN<D, C>(tupleAccessor, children);
    }


    public static <D, C, V1, V2> StorageNodeMutable<D, C, Alt2<V1, V2>> alt2(
            StorageNodeMutable<D, C, V1> child1,
            StorageNodeMutable<D, C, V2> child2
            ) {

        // TODO Validate that all children use the same tuple accessor
        TupleAccessor<D, C> tupleAccessor = child1.getTupleAccessor();
        return new StorageNodeAlt2<D, C, V1, V2>(tupleAccessor, child1, child2);
    }


    public static <D, C, V1, V2, V3> StorageNodeMutable<D, C, Alt3<V1, V2, V3>> alt3(
            StorageNodeMutable<D, C, V1> child1,
            StorageNodeMutable<D, C, V2> child2,
            StorageNodeMutable<D, C, V3> child3
            ) {

        // TODO Validate that all children use the same tuple accessor
        TupleAccessor<D, C> tupleAccessor = child1.getTupleAccessor();
        return new StorageNodeAlt3<D, C, V1, V2, V3>(tupleAccessor, child1, child2, child3);
    }


    /**
     * A wrapper for a {@link StorageNodeMutable#add(Object, Object)} that allows
     * for running a post processing action <i>after</i> a regular insert.
     *
     *
     * @param <D>
     * @param <C>
     * @param <V>
     * @param delegate
     * @param postProcessor
     * @return
     */
    public static <D, C, V> StorageNodeMutable<D, C, V> postProcessAdd(
            StorageNodeMutable<D, C, V> delegate,
            BiConsumer<V, D> postProcessor)
    {
        return new StorageNodeMutableForwardingBase<D, C, V, StorageNodeMutable<D,C,V>>(delegate) {
            @Override
            public boolean add(V store, D tupleLike) {
                boolean result = super.add(store, tupleLike);

                postProcessor.accept(store, tupleLike);

                return result;
            }
        };
    }


    /**
     * Canonicalization maps all equivalent tuples and components w.r.t. .equals() to
     * canonical instances such that '==' becomes sufficient for equality checks.
     *
     * @param <D>
     * @param <C>
     * @param <V>
     * @param <X>
     * @param delegate
     * @return
     */
    public static <D, C, V, X extends StorageNodeMutable<D, C, V>>
        StorageNodeWrapperCodec<D, C, V, X> wrapWithCanonicalization(
            X delegate
            ) {

        TupleCodec<D, C, D, C> tupleCodec = TupleCodecCanonical.create(delegate.getTupleAccessor());

        return new StorageNodeWrapperCodec<D, C, V, X>(
            delegate,
            tupleCodec
            );
    }


    /**
     * DON'T USE ; maintain a TupleCodec separately instead
     *
     * @param <D1> The source domain tuple type, e.g. Triple
     * @param <C1> The source domain component type, e.g. Node
     * @param <D2> The target domain tuple type, e.g. int[]
     * @param <C2> The target domain component type, e.g. Integer
     * @param <V>  The type of the store structure being wrapped; must be based on D2 and C2
     * @param <X>  The type of the storage being wrapped
     */
    public static <D1, C1, D2, C2, V, X extends StorageNodeMutable<D2, C2, V>>
        StorageNodeMutable<D1, C1, V> wrapWithDictionary(
            X delegate,
            TupleAccessor<D1, C1> sourceTupleAccessor
            ) {

        return new StorageNodeDictionary<D1, C1, D2, C2, V, X>(
            delegate,
            HashBiMap.create(),
            sourceTupleAccessor,
            delegate.getTupleAccessor()
            );
    }
}


