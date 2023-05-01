package org.aksw.commons.index;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.aksw.commons.index.core.StorageNodeMutable;
import org.aksw.commons.index.util.CMap;
import org.aksw.commons.index.util.CMapSupplier;
import org.aksw.commons.index.util.MapSuppliers;
import org.aksw.commons.tuple.accessor.TupleAccessorForList;
import org.aksw.commons.tuple.bridge.TupleBridge;
import org.junit.Test;

public class TestCMap {
    @Test
    public void test() {
        TupleBridge<List<String>, String> tupleAccessor = new TupleAccessorForList<>(2);

        CMapSupplier<Boolean> mapSupplier = MapSuppliers.wrapAsCMap(HashMap::new, () -> false);// .wrapAsCMap();

        // result = StorageComposers.innerMap(tupleAccessor.ordinalOfKey(indexVar), mapSupplier, result);
         StorageNodeMutable<List<String>, String, CMap<String, List<String>, Boolean>> storage = CStorageComposers.leafMap(0, mapSupplier, tupleAccessor);

         CMap<String, List<String>, Boolean> store = storage.newStore();

         storage.add(store, Arrays.asList("hello", "world"));

         storage.streamEntries(store).forEach(x -> System.out.println(x));
         // storage.streamerForKeyAndSubStoreAlts(null, (t, i) -> null).stream(store).forEach(x -> System.out.println(x));

         System.out.println(store);
    }
}
