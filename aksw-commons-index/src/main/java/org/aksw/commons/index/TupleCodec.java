package org.aksw.commons.index;

import org.aksw.commons.tuple.TupleAccessor;

/**
 * Component-wise tuple codec.
 * Can be used to map tuples between tuples with different component types,
 * such RDF term to/from integer. 
 * 
 * @author raven
 *
 * @param <D1>
 * @param <C1>
 * @param <D2>
 * @param <C2>
 */
public interface TupleCodec<D1, C1, D2, C2> {

    C2 encodeComponent(C1 c1);

    C1 decodeComponent(C2 c2);

    D2 encodeTuple(D1 sourceTuple);

    D1 decodeTuple(D2 targetTuple);

    TupleAccessor<D1, C1> getSourceTupleAccessor();

    TupleAccessor<D2, C2> getTargetTupleAccessor();

    /**
     * This method can be used as a TupleAccessorCore
     *
     * @param d1
     * @param idx
     * @return
     */
    C2 getEncodedComponent(D1 d1, int idx);

    /**
     * This method can be used as a TupleAccessorCore
     *
     * @param d1
     * @param idx
     * @return
     */
    C1 getDecodedComponent(D2 d2, int idx);
}
