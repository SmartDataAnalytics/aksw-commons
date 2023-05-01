package org.aksw.commons.tuple.finder;

import java.util.stream.Stream;

import org.aksw.commons.tuple.accessor.TupleAccessor;
import org.aksw.commons.tuple.bridge.TupleBridge;

/** Interface for basic matching of tuples */
public interface TupleFinder<D, C> {

    /** Get the object that provides tuple views over the backing domain tuples of type D */
    TupleBridge<D, C> getTupleBridge();

    <X> Stream<D> find(X tuple, TupleAccessor<? super X, ? extends C> accessor);

    default Stream<D> find(D tuple) {
        return find(tuple, getTupleBridge());
    }

    /** The dimension of the tuples accessible by this class */
    default int getDimension() {
        return getTupleBridge().getDimension();
    }

    default <X> boolean contains(X tuple, TupleAccessor<? super X, ? extends C> accessor) {
        boolean result;
        try (Stream<D> stream = find(tuple, accessor)) {
            result = stream.findFirst().isPresent();
        }
        return result;
    }

    default Stream<D> find(@SuppressWarnings("unchecked") C... args) {
        return find(args, (arg, i) -> args[i]);
    }

    default boolean contains(@SuppressWarnings("unchecked") C... args) {
        return contains(args, (arg, i) -> args[i]);
    }

//    default TupleFinder4<D, C> asTupleFinder4() {
//
//    }
}
