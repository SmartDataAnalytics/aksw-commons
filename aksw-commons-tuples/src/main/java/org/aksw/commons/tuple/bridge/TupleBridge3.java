package org.aksw.commons.tuple.bridge;

import org.aksw.commons.tuple.accessor.TupleAccessor;

public interface TupleBridge3<D, C>
    extends TupleBridge<D, C>
{
    D build(C s, C p, C o);

    @Override
    default int getDimension() {
        return 3;
    }

    default C getArg0(D obj) {
        return get(obj, 0);
    }

    default C getArg1(D obj) {
        return get(obj, 1);
    }

    default C getArg2(D obj) {
        return get(obj, 2);
    }

    @Override
    default <T> D build(T obj, TupleAccessor<? super T, ? extends C> accessor) {
        C s = accessor.get(obj, 0);
        C p = accessor.get(obj, 1);
        C o = accessor.get(obj, 2);
        D result = build(s, p, o);
        return result;
    }
}
