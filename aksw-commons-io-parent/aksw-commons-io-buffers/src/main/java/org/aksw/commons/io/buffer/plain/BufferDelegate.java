package org.aksw.commons.io.buffer.plain;

import org.aksw.commons.io.buffer.array.BufferLikeDelegate;

public interface BufferDelegate<A>
	extends BufferLikeDelegate<A, Buffer<A>>, Buffer<A>
{
}
