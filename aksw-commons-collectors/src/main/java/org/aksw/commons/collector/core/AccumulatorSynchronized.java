package org.aksw.commons.collector.core;

import org.aksw.commons.collector.domain.Accumulator;

public class AccumulatorSynchronized<I, E, O>
    implements Accumulator<I, E, O>
{
    protected Accumulator<I, E, O> delegate;

    public AccumulatorSynchronized(Accumulator<I, E, O> delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public synchronized void accumulate(I input, E env) {
        delegate.accumulate(input, env);
    }

    @Override
    public synchronized O getValue() {
        return delegate.getValue();
    }
}
