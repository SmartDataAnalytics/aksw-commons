package org.aksw.commons.collector.core;

import org.aksw.commons.collector.domain.Accumulator;

public interface AccWrapper<I, E, O, SUBACC>
    extends Accumulator<I, E, O>
{
    SUBACC getSubAcc();
}
