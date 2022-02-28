package org.aksw.commons.buffer.range;

public interface RangeBufferDelegateMutable<A>
	extends RangeBufferDelegate<A>
{
	void setDelegate(RangeBuffer<A> delegate);
}
