package org.aksw.commons.collector.domain;

public interface Accumulator<I, O> {
    void accumulate(I input);

    O getValue();
}
