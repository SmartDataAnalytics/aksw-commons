package org.aksw.commons.rx.cache.range;

import org.aksw.commons.util.array.Buffer;

public interface BufferDelegate<A>
	extends BufferLikeDelegate<A, Buffer<A>>, Buffer<A>
{
}
