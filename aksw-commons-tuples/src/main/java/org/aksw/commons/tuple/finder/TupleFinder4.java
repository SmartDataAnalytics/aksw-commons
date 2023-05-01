package org.aksw.commons.tuple.finder;

import java.util.stream.Stream;

import org.aksw.commons.tuple.accessor.TupleAccessor;
import org.aksw.commons.tuple.bridge.TupleBridge4;

public interface TupleFinder4<D, C>
    extends TupleFinder<D, C>
{
    Stream<D> find(C g, C s, C p, C o);

    @Override
    default int getDimension() {
        return 4;
    }

    @Override
    default <X> Stream<D> find(X tuple, TupleAccessor<? super X, ? extends C> accessor) {
        C g = accessor.get(tuple, 0);
        C s = accessor.get(tuple, 1);
        C p = accessor.get(tuple, 2);
        C o = accessor.get(tuple, 3);
        Stream<D> result = find(g, s, p, o);
        return result;
    }

    default boolean contains(C g, C s, C p, C o) {
        boolean result;
        try (Stream<D> stream = find(g, s, p, o)) {
            result = stream.findFirst().isPresent();
        }
        return result;
    }

    @Override
    TupleBridge4<D, C> getTupleBridge();
}
