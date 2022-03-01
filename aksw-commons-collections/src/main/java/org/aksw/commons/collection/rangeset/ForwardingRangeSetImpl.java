package org.aksw.commons.collection.rangeset;

import com.google.common.collect.RangeSet;

public class ForwardingRangeSetImpl<T extends Comparable<T>>
	extends ForwardingRangeSetBase<T>
{

	public ForwardingRangeSetImpl(RangeSetDelegate<T> delegate) {
		super(delegate);
	}

	void setDelegate(RangeSet<T> newDelegate) {
		this.delegate = newDelegate;
	}
}
