package org.aksw.commons.rx.cache.range;

import org.aksw.commons.buffer.plain.Buffer;

public interface BufferDelegate<A>
	extends BufferLikeDelegate<A, Buffer<A>>, Buffer<A>
{
}
