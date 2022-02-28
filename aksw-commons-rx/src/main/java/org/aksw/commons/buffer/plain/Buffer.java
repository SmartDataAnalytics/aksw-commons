package org.aksw.commons.buffer.plain;

import org.aksw.commons.util.array.BufferLike;

import com.google.common.math.LongMath;

public interface Buffer<A>
	extends BufferLike<A>
{
	/** Create a sub-buffer view of this buffer */
	default Buffer<A> slice(long offset, long length) {
		if (LongMath.checkedAdd(offset, length) > getCapacity()) {
			throw new RuntimeException("Sub-buffer extends over capacity of this buffer");
		}
		
		// TODO If this buffer is already a sub-buffer then prevent wrapping it again for performance
		
		return new SubBuffer<A>(this, offset, length);
	}
}
