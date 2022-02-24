package org.aksw.commons.rx.cache.range;

import java.io.IOException;

import org.aksw.commons.util.array.Buffer;
import org.aksw.commons.util.array.BufferLike;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public interface RangeBuffer<A>
    extends BufferLike<A>
{
    /** A set of ranges from which reading is valid. The range set may be shared among several range buffers and may thus include
     *  ranges outside of the range formed by the buffer's offset and capacity */
    RangeSet<Long> getRanges();

    /** The offset within the range set where this buffer starts; may be null if the offset cannot be represented in a single value such as
     * in a union of two buffers which have different offsets */
    Long getOffsetInRanges();

    Buffer<A> getBackingBuffer();

    /** For a given buffer-local range return available ranges in global space */
    // RangeSet<Long> getAvailableGlobalRanges(Range<Long> bufferRange);

    /** Return a set of contributions by this buffer for the given lookup range
     *  While this method does not expose which parts of the global range are covered,
     *  this method allows to check whether there are any gaps in the read */
    RangeSet<Long> getCoveredRanges(Range<Long> localRange);


    // RangeSet<Long> getGlobalRanges(Range<Long> localRange);


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

        RangeSet<Long> validReadRanges = src.getCoveredRanges(readRange);

        // TODO Cache buffer
        int n = 4 * 1024;
        A buffer = tgt.getArrayOps().create(n);


        // for (Range<Long> range :  src.getRanges().subRangeSet(readRange).asRanges()) {
        for (Range<Long> range : validReadRanges.asRanges()) {
            ContiguousSet<Long> cs = ContiguousSet.create(range, DiscreteDomain.longs());
            int remaining = cs.size();
            long first = cs.first();
            while (remaining > 0) {
                int x = Math.min(remaining, n);
                src.readInto(buffer, 0, first, x);
                long o = srcOffset - tgtOffset + first;
                tgt.write(o, buffer, 0, x);
                remaining -= x;
                first += x;
            }
        }
    }

    @Override
    default RangeBuffer<A> slice(long offset, long length) {
        //RangeSet<Long> subRangeSet = getRanges().subRangeSet(Range.closedOpen(offset, offset + length));

        // The backing buffer's position 0 is at the global offset
        long globalOffset = getOffsetInRanges();
        long relativeOffset = offset - globalOffset;
        long newGlobalOffset = globalOffset + offset;

        return new RangeBufferImpl<>(getRanges(), newGlobalOffset, getBackingBuffer().slice(relativeOffset, length));
    }
}
