package org.aksw.commons.collector.domain;

public interface Accumulator<I, E, O> {
    void accumulate(I input, E env);

    default void accumulate(I input) {
        accumulate(input, null);
    }

    O getValue();
}
