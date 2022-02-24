package org.aksw.commons.rx.cache.range;

import java.io.IOException;

import org.aksw.commons.util.array.ArrayOps;
import org.aksw.commons.util.array.BufferLike;

public interface BufferLikeDelegate<A, D extends BufferLike<A>>
	extends BufferLike<A>
{
	D getDelegate();

	@Override
	default void write(long offsetInBuffer, A arrayWithItemsOfTypeT, int arrOffset, int arrLength)
			throws IOException {
		getDelegate().write(offsetInBuffer, arrayWithItemsOfTypeT, arrOffset, arrLength);
	}

	@Override
	default int readInto(A tgt, int tgtOffset, long srcOffset, int length) throws IOException {
		return getDelegate().readInto(tgt, tgtOffset, srcOffset, length);
	}

	@Override
	default ArrayOps<A> getArrayOps() {
		return getDelegate().getArrayOps();
	}

	@Override
	default long getCapacity() {
		return getDelegate().getCapacity();
	}

}
