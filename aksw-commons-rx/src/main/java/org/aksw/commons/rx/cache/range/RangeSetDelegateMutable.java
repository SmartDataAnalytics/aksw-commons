package org.aksw.commons.rx.cache.range;

import com.google.common.collect.RangeSet;

public interface RangeSetDelegateMutable<T extends Comparable<T>>
	extends RangeSetDelegate<T>
{
	void setDelegate(RangeSet<T> delegate);
}
