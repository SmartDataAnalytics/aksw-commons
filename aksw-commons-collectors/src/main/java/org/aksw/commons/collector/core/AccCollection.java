package org.aksw.commons.collector.core;

import java.io.Serializable;
import java.util.Collection;

import org.aksw.commons.collector.domain.Accumulator;

/** Can be used with AggNatural */
public class AccCollection<I, C extends Collection<I>>
	implements Accumulator<I, C>, Serializable
{
	private static final long serialVersionUID = 0;

	protected C value;
	
	public AccCollection(C value) {
		super();
		this.value = value;
	}
	
	@Override
	public void accumulate(I item) {
		value.add(item);
	}
	
	@Override
	public C getValue() {
		return value;
	}
	
}