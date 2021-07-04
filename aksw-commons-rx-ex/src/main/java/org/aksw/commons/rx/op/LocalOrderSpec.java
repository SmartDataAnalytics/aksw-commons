package org.aksw.commons.rx.op;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface LocalOrderSpec<T, S> {
    Function<? super S, ? extends S> getIncrementSeqId();
    BiFunction<? super S, ? super S, ? extends Number> getDistanceFn();
    Function<? super T, ? extends S> getExtractSeqId();
}