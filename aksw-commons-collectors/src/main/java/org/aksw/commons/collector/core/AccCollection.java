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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccCollection other = (AccCollection) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}