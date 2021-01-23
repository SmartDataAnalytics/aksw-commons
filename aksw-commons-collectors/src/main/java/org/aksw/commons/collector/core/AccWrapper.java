package org.aksw.commons.collector.core;

import org.aksw.commons.collector.domain.Accumulator;

public interface AccWrapper<I, O, SUBACC>
	extends Accumulator<I, O>
{
	SUBACC getSubAcc();
}
