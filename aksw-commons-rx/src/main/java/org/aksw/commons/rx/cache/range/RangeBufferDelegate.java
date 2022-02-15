package org.aksw.commons.rx.cache.range;

import com.google.common.collect.RangeSet;

public interface RangeBufferDelegate<A>
	extends RangeBuffer<A>, BufferLikeDelegate<A, RangeBuffer<A>>
{

	@Override
	default RangeSet<Long> getRanges() {
		return getDelegate().getRanges();
	}

	@Override
	default Long getOffsetInRanges() {
		return getDelegate().getOffsetInRanges();
	}

	@Override
	default RangeBuffer<A> slice(long offset, long length) {
		return getDelegate().slice(offset, length);
	}
}
