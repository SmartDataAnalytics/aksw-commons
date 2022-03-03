package org.aksw.commons.index;

import org.aksw.commons.tuple.TupleAccessor;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public abstract class TupleCodecDictionary<D1, C1, D2, C2>
    implements TupleCodec<D1, C1, D2, C2> {
    protected BiMap<C1, C2> dictionary;

    protected TupleAccessor<D1, C1> sourceTupleAccessor;
    protected TupleAccessor<D2, C2> targetTupleAccessor;

//    protected TupleAccessor<D1, C2> encodingTupleAccessor;
//    protected TupleAccessor<D2, C1> decodingTupleAccessor;

    public TupleCodecDictionary(
//            BiMap<C1, C2> dictionary,
            TupleAccessor<D1, C1> sourceTupleAccessor,
            TupleAccessor<D2, C2> targetTupleAccessor
            ) {
        super();
        this.dictionary = HashBiMap.create(); //dictionary;

        this.sourceTupleAccessor = sourceTupleAccessor;
        this.targetTupleAccessor = targetTupleAccessor;
    }

    protected abstract C2 makeEntry(C1 c1);


    public static <D, C> TupleCodec<D, C, int[], Integer> createForInts(
            TupleAccessor<D, C> source,
            TupleAccessor<int[], Integer> target
            ) {

        return new TupleCodecDictionary<D, C, int[], Integer>(source, target) {
            @Override
            protected Integer makeEntry(C c1) {
                int r = dictionary.size();
                dictionary.put(c1, r);
                return r;
            }
        };
    }

    public C2 getEncodedComponent(D1 d1, int idx) {
        C1 c1 = sourceTupleAccessor.get(d1, idx);
        C2 result = encodeComponent(c1);
        return result;
    }

    public C1 getDecodedComponent(D2 d2, int idx) {
        C2 c2 = targetTupleAccessor.get(d2, idx);
        C1 result = decodeComponent(c2);
        return result;
    }

    public TupleAccessor<D1, C1> getSourceTupleAccessor() {
        return sourceTupleAccessor;
    }

    public TupleAccessor<D2, C2> getTargetTupleAccessor() {
        return targetTupleAccessor;
    }

    @Override
    public C2 encodeComponent(C1 c1) {
        C2 result = dictionary.computeIfAbsent(c1, c -> makeEntry(c));
        return result;
    }

    @Override
    public C1 decodeComponent(C2 c2) {
        C1 result = dictionary.inverse().get(c2);
        return result;
    }


    @Override
    public D2 encodeTuple(D1 sourceTuple) {
        D2 result = targetTupleAccessor.restore(sourceTuple, (st, i) -> {
            C1 c1 = sourceTupleAccessor.get(st, i);
            C2 c2 = encodeComponent(c1);
            return c2;
        });
        return result;
    }


    @Override
    public D1 decodeTuple(D2 targetTuple) {
        D1 result = sourceTupleAccessor.restore(targetTuple, (tt, i) -> {
            C2 c2 = targetTupleAccessor.get(tt, i);
            C1 c1 = dictionary.inverse().get(c2);

            // TODO What if c2 is not present in the dictionary? Raise an exception?
            return c1;
        });
        return result;
    }

}
