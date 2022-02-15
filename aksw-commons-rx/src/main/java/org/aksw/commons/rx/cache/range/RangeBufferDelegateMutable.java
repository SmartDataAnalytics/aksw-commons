package org.aksw.commons.rx.cache.range;

public interface RangeBufferDelegateMutable<A>
	extends RangeBufferDelegate<A>
{
	void setDelegate(RangeBuffer<A> delegate);
}
