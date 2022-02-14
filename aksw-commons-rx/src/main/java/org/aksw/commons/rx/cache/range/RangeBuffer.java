package org.aksw.commons.rx.cache.range;

import java.io.IOException;

import org.aksw.commons.util.array.Buffer;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public interface RangeBuffer<A>
	extends Buffer<A>
{	
	/** A set of ranges from which reading is valid. The range set may be shared among several range buffers and may thus include
	 *  ranges outside of the range formed by the buffer's offset and capacity */
	RangeSet<Long> getRanges();

	/** The offset within the range set where this buffer starts; may be null if the offset cannot be represented in a single value such as
	 * in a union of two buffers which have different offsets */
	Long getOffsetInRanges();
	
	default void transferFrom(long thisOffset, RangeBuffer<A> other, long otherOffset, long length) throws IOException {
//		Range<Long> readRange = Range.closedOpen(otherOffset, otherOffset + length);
//		
//		int n = 4 * 1024;
//		A buffer = getArrayOps().create(n);
//		for (Range<Long> range :  other.getRanges().subRangeSet(readRange).asRanges()) {
//			ContiguousSet<Long> cs = ContiguousSet.create(range, DiscreteDomain.longs());
//			int remaining = cs.size();
//			long first = cs.first();
//			while (remaining > 0) {
//				int x = Math.min(remaining, n);
//				other.readInto(buffer, 0, first, x);				
//				long o = thisOffset - otherOffset + first;
//				putAll(o, buffer, 0, x);
//				remaining -= x;
//				first += x;
//			}
//		}
		transfer(other, otherOffset, this, thisOffset, length);
	}
	
	default void transferTo(long thisOffset, RangeBuffer<A> other, long otherOffset, long length) throws IOException {
		transfer(this, thisOffset, other, otherOffset, length);		
	}
	
	
	public static <A> void transfer(RangeBuffer<A> src, long srcOffset, RangeBuffer<A> tgt, long tgtOffset, long length) throws IOException {
		Range<Long> readRange = Range.closedOpen(srcOffset, srcOffset + length);
		
		int n = 4 * 1024;
		A buffer = tgt.getArrayOps().create(n);
		for (Range<Long> range :  src.getRanges().subRangeSet(readRange).asRanges()) {
			ContiguousSet<Long> cs = ContiguousSet.create(range, DiscreteDomain.longs());
			int remaining = cs.size();
			long first = cs.first();
			while (remaining > 0) {
				int x = Math.min(remaining, n);
				src.readInto(buffer, 0, first, x);				
				long o = srcOffset - tgtOffset + first;
				tgt.putAll(o, buffer, 0, x);
				remaining -= x;
				first += x;
			}
		}		
	}
	
	@Override
	default RangeBuffer<A> slice(long offset, long length) {
		//RangeSet<Long> subRangeSet = getRanges().subRangeSet(Range.closedOpen(offset, offset + length));
		long o = getOffsetInRanges();
		long newOffset = o + offset;
		return new RangeBufferImpl<>(getRanges(), offset, Buffer.super.slice(newOffset, length));
	}
}
