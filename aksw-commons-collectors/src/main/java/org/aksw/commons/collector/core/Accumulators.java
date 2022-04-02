package org.aksw.commons.collector.core;

import org.aksw.commons.collector.domain.Accumulator;

public class Accumulators {
    public static <I, O> Accumulator<I, O> synchronize(Accumulator<I, O> accumulator) {
        Accumulator<I, O> result = accumulator instanceof AccumulatorSynchronized
                ? accumulator
                : new AccumulatorSynchronized<>(accumulator);

        return result;
    }
}
