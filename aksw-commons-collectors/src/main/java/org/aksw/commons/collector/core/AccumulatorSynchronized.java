package org.aksw.commons.collector.core;

import org.aksw.commons.collector.domain.Accumulator;

public class AccumulatorSynchronized<I, O>
    implements Accumulator<I, O>
{
    protected Accumulator<I, O> delegate;

    public AccumulatorSynchronized(Accumulator<I, O> delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public synchronized void accumulate(I input) {
        delegate.accumulate(input);
    }

    @Override
    public synchronized O getValue() {
        return delegate.getValue();
    }
}
