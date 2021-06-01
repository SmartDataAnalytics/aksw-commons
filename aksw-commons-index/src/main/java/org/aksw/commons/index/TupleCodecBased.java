package org.aksw.commons.index;

public interface TupleCodecBased<D1, C1, D2, C2> {
    TupleCodec<D1, C1, D2, C2> getTupleCodec();
}
