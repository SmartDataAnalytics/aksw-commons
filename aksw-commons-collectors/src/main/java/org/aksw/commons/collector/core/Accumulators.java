package org.aksw.commons.collector.core;

import org.aksw.commons.collector.domain.Accumulator;

public class Accumulators {
    public static <I, E, O> Accumulator<I, E, O> synchronize(Accumulator<I, E, O> accumulator) {
        Accumulator<I, E, O> result = accumulator instanceof AccumulatorSynchronized
                ? accumulator
                : new AccumulatorSynchronized<>(accumulator);

        return result;
    }
}
