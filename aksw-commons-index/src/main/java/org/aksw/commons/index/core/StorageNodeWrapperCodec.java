package org.aksw.commons.index.core;

import org.aksw.commons.index.TupleCodec;

public class StorageNodeWrapperCodec<D, C, V, X extends StorageNodeMutable<D,C,V>>
    extends StorageNodeMutableForwardingBase<D, C, V, X>
{
    protected X delegate;
    protected TupleCodec<D, C, D, C> codec;

    public StorageNodeWrapperCodec(X delegate, TupleCodec<D, C, D, C> codec) {
        super(delegate);
        this.delegate = delegate;
        this.codec = codec;
    }

    public TupleCodec<D, C, D, C> getCodec() {
        return codec;
    }

    @Override
    public boolean add(V store, D tupleLike) {
        D encodedTuple = codec.encodeTuple(tupleLike);

        return super.add(store, encodedTuple);
    }

    @Override
    public boolean remove(V store, D tupleLike) {
        D encodedTuple = codec.encodeTuple(tupleLike);

        return super.remove(store, encodedTuple);
    }
}
