package org.aksw.commons.collection.rangeset;

import com.google.common.collect.RangeSet;

public class ForwardingRangeSetBase<T extends Comparable<T>>
	implements RangeSetDelegate<T>
{
	protected RangeSet<T> delegate;

	public ForwardingRangeSetBase(RangeSetDelegate<T> delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public RangeSet<T> getDelegate() {
		return delegate;
	}

}
