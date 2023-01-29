package org.aksw.commons.index;

import java.util.HashMap;
import java.util.Map;

import org.aksw.commons.tuple.bridge.TupleBridge;

public abstract class TupleCodecCanonical<D, C>
    implements TupleCodec<D, C, D, C>
{
    protected Map<C, C> canonicalMap;

    protected TupleBridge<D, C> tupleAccessor;

    //protected TupleAccessor<D1, C2> encodingTupleAccessor;
    //protected TupleAccessor<D2, C1> decodingTupleAccessor;

    public TupleCodecCanonical(
    //        BiMap<C1, C2> dictionary,
            TupleBridge<D, C> tupleAccessor
            ) {
        super();
        this.canonicalMap = new HashMap<>(); //dictionary;

        this.tupleAccessor = tupleAccessor;
    }

    protected abstract C makeEntry(C c1);


    public static <D, C> TupleCodec<D, C, D, C> create(
            TupleBridge<D, C> tupleAccessor
            ) {

        return new TupleCodecCanonical<D, C>(tupleAccessor) {
            @Override
            protected C makeEntry(C c1) {
                canonicalMap.put(c1, c1);
                return c1;
            }
        };
    }

    public C getEncodedComponent(D d1, int idx) {
        C c1 = tupleAccessor.get(d1, idx);
        C result = encodeComponent(c1);
        return result;
    }

    public C getDecodedComponent(D d, int idx) {
        C c2 = tupleAccessor.get(d, idx);
        //C result = decodeComponent(c2);
        return c2;
    }

    public TupleBridge<D, C> getSourceTupleAccessor() {
        return tupleAccessor;
    }

    public TupleBridge<D, C> getTargetTupleAccessor() {
        return tupleAccessor;
    }

    @Override
    public C encodeComponent(C c1) {
        C result = canonicalMap.computeIfAbsent(c1, c -> makeEntry(c));
        return result;
    }

    @Override
    public C decodeComponent(C c2) {
        //C1 result = dictionary.inverse().get(c2);
        return c2;
    }


    @Override
    public D encodeTuple(D sourceTuple) {
        D result = tupleAccessor.build(sourceTuple, (st, i) -> {
            C c1 = tupleAccessor.get(st, i);
            C c2 = encodeComponent(c1);
            return c2;
        });
        return result;
    }


    @Override
    public D decodeTuple(D targetTuple) {
        return targetTuple;
//        D1 result = sourceTupleAccessor.restore(targetTuple, (tt, i) -> {
//            C2 c2 = targetTupleAccessor.get(tt, i);
//            C1 c1 = dictionary.inverse().get(c2);
//
//            // TODO What if c2 is not present in the dictionary? Raise an exception?
//            return c1;
//        });
//        return result;
    }

}