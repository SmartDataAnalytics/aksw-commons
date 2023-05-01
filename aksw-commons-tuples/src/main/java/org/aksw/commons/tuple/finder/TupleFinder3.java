package org.aksw.commons.tuple.finder;

import java.util.stream.Stream;

import org.aksw.commons.tuple.accessor.TupleAccessor;
import org.aksw.commons.tuple.bridge.TupleBridge3;

public interface TupleFinder3<D, C>
    extends TupleFinder<D, C>
{
    Stream<D> find(C s, C p, C o);

    @Override
    default int getDimension() {
        return 3;
    }

    @Override
    default <X> Stream<D> find(X tuple, TupleAccessor<? super X, ? extends C> accessor) {
        C s = accessor.get(tuple, 0);
        C p = accessor.get(tuple, 1);
        C o = accessor.get(tuple, 2);
        Stream<D> result = find(s, p, o);
        return result;
    }

    default boolean contains(C s, C p, C o) {
        boolean result;
        try (Stream<D> stream = find(s, p, o)) {
            result = stream.findFirst().isPresent();
        }
        return result;
    }

    @Override
    TupleBridge3<D, C> getTupleBridge();
}
