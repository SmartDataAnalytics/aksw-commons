package org.aksw.commons.io.buffer.range;

public interface RangeBufferDelegateMutable<A>
	extends RangeBufferDelegate<A>
{
	void setDelegate(RangeBuffer<A> delegate);
}
