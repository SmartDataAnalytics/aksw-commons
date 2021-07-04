package org.aksw.commons.rx.op;

import java.util.function.BiFunction;
import java.util.function.Function;

public class LocalOrderBase<T, S> {
    protected Function<? super S, ? extends S> incrementSeqId;
    protected BiFunction<? super S, ? super S, ? extends Number> distanceFn;
    protected Function<? super T, ? extends S> extractSeqId;

    public LocalOrderBase(
            Function<? super S, ? extends S> incrementSeqId,
            BiFunction<? super S, ? super S, ? extends Number> distanceFn,
            Function<? super T, ? extends S> extractSeqId) {
        super();
        this.incrementSeqId = incrementSeqId;
        this.distanceFn = distanceFn;
        this.extractSeqId = extractSeqId;
    }

    public LocalOrderBase(LocalOrderBase<T, S> other) {
        this(other.incrementSeqId, other.distanceFn, other.extractSeqId);
    }

    public LocalOrderBase(LocalOrderSpec<T, S> other) {
        this(other.getIncrementSeqId(), other.getDistanceFn(), other.getExtractSeqId());
    }

}