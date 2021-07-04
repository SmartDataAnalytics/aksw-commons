package org.aksw.commons.rx.op;

import java.util.function.BiFunction;
import java.util.function.Function;

public class LocalOrderSpecImpl<T, S>
    extends LocalOrderBase<T, S>
    implements LocalOrderSpec<T, S>
{

    public LocalOrderSpecImpl(
            Function<? super S, ? extends S> incrementSeqId,
            BiFunction<? super S, ? super S, ? extends Number> distanceFn,
            Function<? super T, ? extends S> extractSeqId
        ) {
        super(incrementSeqId, distanceFn, extractSeqId);
    }

    @Override
    public Function<? super S, ? extends S> getIncrementSeqId() {
        return incrementSeqId;
    }

    @Override
    public BiFunction<? super S, ? super S, ? extends Number> getDistanceFn() {
        return distanceFn;
    }

    @Override
    public Function<? super T, ? extends S> getExtractSeqId() {
        return extractSeqId;
    }


    public static <T> LocalOrderSpecImpl<T, Long> forLong(Function<? super T, ? extends Long> extractSeqId) {
        return new LocalOrderSpecImpl<T, Long>(
                id -> Long.valueOf(id.longValue() + 1l),
                (a, b) -> a - b,
                extractSeqId);
    }

    public static <T, S extends Comparable<S>> LocalOrderSpecImpl<T, S> wrap(Function<? super S, ? extends S> incrementSeqId, BiFunction<? super S, ? super S, ? extends Number> distanceFn, Function<? super T, ? extends S> extractSeqId) {
        return new LocalOrderSpecImpl<T, S>(incrementSeqId, distanceFn, extractSeqId);
    }

    public static <T, S extends Comparable<S>> LocalOrderSpecImpl<T, S> create(
            S initialExpectedSeqId,
            Function<? super S, ? extends S> incrementSeqId,
            BiFunction<? super S, ? super S, ? extends Number> distanceFn,
            Function<? super T, ? extends S> extractSeqId) {
        return new LocalOrderSpecImpl<T, S>(incrementSeqId, distanceFn, extractSeqId);
    }


}