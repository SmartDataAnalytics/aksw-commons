package org.aksw.commons.tuple.bridge;

import org.aksw.commons.tuple.accessor.TupleAccessor;

public interface TupleBridge4<D, C>
    extends TupleBridge<D, C>
{
    D build(C g, C s, C p, C o);

    @Override
    default int getDimension() {
        return 4;
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

    default C getArg3(D obj) {
        return get(obj, 3);
    }

    @Override
    default <T> D build(T obj, TupleAccessor<? super T, ? extends C> accessor) {
        C g = accessor.get(obj, 0);
        C s = accessor.get(obj, 1);
        C p = accessor.get(obj, 2);
        C o = accessor.get(obj, 3);
        D result = build(g, s, p, o);
        return result;
    }
}
