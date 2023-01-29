package org.aksw.commons.index.core;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.commons.tuple.accessor.TupleAccessor;
import org.aksw.commons.tuple.bridge.TupleBridge;
import org.aksw.commons.util.stream.Streamer;

import com.google.common.collect.BiMap;

/**
 * A wrapper for a storage node that adds a dictionary for the components
 * Can be used to map e.g. Nodes to Integers or canonical instances
 *
 * Maybe this fits better on a TupleAccessor?
 *
 * The issue with storage nodes is that they are stateless. So if we wanted to wrap an exiting structure
 * with a dictionary we would end up with:
 *
 * Dictionary<Alt3<...>, Map<Node, Integer>
 *
 * Probably it would not be too bad because we could still easily get the nested map structure out
 *
 * Actually, why shouldn't the storage node have a state?
 *
 *
 * StorageNode<int[], int, ?> intStorage;
 * TupleAccessor<Integer> intAccessor;
 *
 * TupleAccessor<Triple> tripleAccessor = TupleAccessorTriple.INSTANCE
 *
 * StorageNodeDictionary<Triple, Node, ?> tripleStorageWrapper =
 *   StorageNodeDictionary.wrap(intStorage, nodeAccessor, (dict, c) -> dict.size(), intAccessor)
 *
 *
 * BiMap<Node, Integer> tripleStorageWrapper.getDictionary();
 *
 * @author raven
 *
 */
public class StorageNodeDictionary<D1, C1, D2, C2, V, X extends StorageNodeMutable<D2, C2, V>>
    implements StorageNodeMutable<D1, C1, V>
    //extends StorageNodeMutableForwarding<D, C, V, StorageNodeMutable<D,C,V>>
{
    protected X delegate;
    protected BiMap<C1, C2> dictionary;
    protected TupleBridge<D1, C1> sourceTupleAccessor;

    // The converting accessor is backed by the tupleAccesssor and the dictionary
    protected TupleBridge<D2, C2> targetTupleAccessor;

    public StorageNodeDictionary(
            X delegate,
            BiMap<C1, C2> dictionary,
            TupleBridge<D1, C1> sourceTupleAccessor,
            TupleBridge<D2, C2> targetTupleAccessor
            ) {
        super();
        this.delegate = delegate;
        this.dictionary = dictionary;

        this.sourceTupleAccessor = sourceTupleAccessor;
        this.targetTupleAccessor = targetTupleAccessor;
    }


    protected C2 makeEntry(C1 c1) {
        return null;
    }

    public D2 mapSourceToTarget(D1 sourceTuple) {
        D2 result = targetTupleAccessor.build(sourceTuple, (st, i) -> {
            C1 c1 = sourceTupleAccessor.get(st, i);
            C2 c2 = dictionary.computeIfAbsent(c1, c -> makeEntry(c));
            return c2;
        });
        return result;
    }


    public D1 mapTargetToSource(D2 targetTuple) {
        D1 result = sourceTupleAccessor.build(targetTuple, (tt, i) -> {
            C2 c2 = targetTupleAccessor.get(tt, i);
            C1 c1 = dictionary.inverse().get(c2);

            // TODO What if c2 is not present in the dictionary? Raise an exception?
            return c1;
        });
        return result;
    }


    @Override
    public boolean add(V store, D1 sourceTuple) {
        D2 targetTuple = mapSourceToTarget(sourceTuple);
        return delegate.add(store, targetTuple);
    }

    @Override
    public boolean remove(V store, D1 sourceTuple) {
        D2 targetTuple = mapSourceToTarget(sourceTuple);
        return delegate.remove(store, targetTuple);
    }

    @Override
    public void clear(V store) {
        delegate.clear(store);
    }


    @Override
    public V newStore() {
        return delegate.newStore();
    }

    @Override
    public boolean isEmpty(V store) {
        return delegate.isEmpty(store);
    }



    @Override
    public List<? extends StorageNode<D1, C1, ?>> getChildren() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public int[] getKeyTupleIdxs() {
        return delegate.getKeyTupleIdxs();
    }


    @Override
    public TupleBridge<D1, C1> getTupleAccessor() {
        return sourceTupleAccessor;
    }


    @Override
    public <T> Streamer<V, C1> streamerForKeysAsComponent(T pattern,
            TupleAccessor<? super T, ? extends C1> accessor) {
        throw new UnsupportedOperationException("not implemented");
    }


    @Override
    public <T> Streamer<V, List<C1>> streamerForKeysAsTuples(T pattern,
            TupleAccessor<? super T, ? extends C1> accessor) {
        throw new UnsupportedOperationException("not implemented");
    }


    @Override
    public <T> Streamer<V, ?> streamerForKeys(T pattern, TupleAccessor<? super T, ? extends C1> accessor) {
        throw new UnsupportedOperationException("not implemented");
    }


    @Override
    public C1 getKeyComponentRaw(Object key, int idx) {
        throw new UnsupportedOperationException("not implemented");
    }


    @Override
    public Object chooseSubStore(V store, int subStoreIdx) {
        // TODO Check for subStoreIdx == 0
        return store;
    }


    @Override
    public <T> Streamer<V, ?> streamerForValues(T pattern, TupleAccessor<? super T, ? extends C1> accessor) {
        throw new UnsupportedOperationException("not implemented");
    }


    @Override
    public <T> Streamer<V, ? extends Entry<?, ?>> streamerForKeyAndSubStoreAlts(T pattern,
            TupleAccessor<? super T, ? extends C1> accessor) {
        throw new UnsupportedOperationException("not implemented");
    }


    @Override
    public <T> Stream<?> streamEntries(V store, T tupleLike, TupleAccessor<? super T, ? extends C1> tupleAccessor) {
        throw new UnsupportedOperationException("not implemented");
    }


}

