package org.aksw.commons.util.array;

import com.google.common.math.LongMath;

public interface Buffer<A>
	extends ArrayWritable<A>, ArrayReadable<A>
{
	ArrayOps<A> getArrayOps();

	
	/** Buffers with 'unlimited' capacity should return Long.MAX_VALUE */
	long getCapacity();


	/** Create a sub-buffer view of this buffer */
	default Buffer<A> slice(long offset, long length) {
		if (LongMath.checkedAdd(offset, length) > getCapacity()) {
			throw new RuntimeException("Sub-buffer extends over capacity of this buffer");
		}
		
		// TODO If this buffer is already a sub-buffer then prevent wrapping it again for performance
		
		return new SubBuffer<A>(this, offset, length);
	}
}
