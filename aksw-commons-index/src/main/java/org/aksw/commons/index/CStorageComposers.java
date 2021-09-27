package org.aksw.commons.index;

import org.aksw.commons.index.core.StorageNodeMutable;
import org.aksw.commons.index.util.CMap;
import org.aksw.commons.index.util.CMapSupplier;
import org.aksw.commons.index.util.CSet;
import org.aksw.commons.index.util.CSetSupplier;
import org.aksw.commons.tuple.TupleAccessor;

public class CStorageComposers {

    public static <D, C, X> StorageNodeMutable<D, C, CSet<D, X>> leafSet(
            CSetSupplier<X> setSupplier,
            TupleAccessor<D, C> tupleAccessor) {
        return StorageComposers.leafSet(setSupplier, tupleAccessor);
    }

    public static <D, C, X> StorageNodeMutable<D, C, CSet<C, X>> leafComponentSet(
            int tupleIdx,
            CSetSupplier<X> setSupplier,
            TupleAccessor<D, C> tupleAccessor) {
        return StorageComposers.leafComponentSet(tupleIdx, setSupplier, tupleAccessor);
    }


    public static <D, C, X> StorageNodeMutable<D, C, CMap<C, D, X>> leafMap(
            int tupleIdx,
            CMapSupplier<X> mapSupplier,
            TupleAccessor<D, C> tupleAccessor) {
        return StorageComposers.leafMap(tupleIdx, mapSupplier, tupleAccessor);
    }

    public static <D, C, V, X> StorageNodeMutable<D, C, CMap<C, V, X>> innerMap(
            int tupleIdx,
            CMapSupplier<X> mapSupplier,
            StorageNodeMutable<D, C, V> child) {
        return StorageComposers.innerMap(tupleIdx, mapSupplier, child);
    }

}
