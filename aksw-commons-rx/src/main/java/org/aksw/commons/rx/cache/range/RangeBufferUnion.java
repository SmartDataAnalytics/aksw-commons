package org.aksw.commons.rx.cache.range;

import java.io.IOException;
import java.util.Set;

import org.aksw.commons.util.array.ArrayOps;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.google.common.primitives.Ints;

/**
 * A union view of two buffers.
 * Writes go into the first one.
 * 
 * @author raven
 *
 * @param <A>
 */
public class RangeBufferUnion<A>
	implements RangeBuffer<A>
{
	protected RangeBuffer<A> first;
	protected RangeBuffer<A> second;

	protected RangeSet<Long> unionRanges;
	
	
	public RangeBufferUnion(RangeBuffer<A> first, RangeBuffer<A> second) {
		super();
		this.first = first;
		this.second = second;
		this.unionRanges = RangeSetUnion.create(first.getRanges(), second.getRanges());
	}

	public static <A> RangeBufferUnion<A> create(RangeBuffer<A> first, RangeBuffer<A> second) {
		return new RangeBufferUnion<>(first, second);
	}
	
	@Override
	public void putAll(long offsetInBuffer, Object arrayWithItemsOfTypeT, int arrOffset, int arrLength) throws IOException {
		first.putAll(offsetInBuffer, arrayWithItemsOfTypeT, arrOffset, arrLength);
	}
	
	@Override
	public void put(long offset, Object item) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public ArrayOps<A> getArrayOps() {
		return first.getArrayOps();
	}
	
	@Override
	public int readInto(A tgt, int tgtOffset, long srcOffset, int length) throws IOException {
		long end = srcOffset + length;		
		Range<Long> totalReadRange = Range.closedOpen(srcOffset, end);
		
		if (!unionRanges.encloses(totalReadRange)) {
			// UnionRangeSet does not yet provide a .complement() view
			Set<Range<Long>> gaps = RangeSetUtils.complement(unionRanges, totalReadRange);
			// RangeSet<Long> gaps = unionRanges.complement().subRangeSet(totalReadRange);
			
			throw new RuntimeException("Attempt to read over gaps at: " + gaps);
		}
		
//		RangeSet<Long> gaps = TreeRangeSet.create();
//		gaps.add(totalReadRange);
//		for (RangeBuffer<A> buffer : buffers) {
//			buffer.getRanges().
//			
//			Set<Range<Long>> reads = buffer.getRanges().subRangeSet(totalReadRange).asRanges();
//			Set<Range<Long>> sreads = RangeSetUtils.difference(second.getRanges().subRangeSet(totalReadRange), first.getRanges());
//			
//		}
		
		Set<Range<Long>> freads = first.getRanges().subRangeSet(totalReadRange).asRanges();
		Set<Range<Long>> sreads = RangeSetUtils.difference(second.getRanges().subRangeSet(totalReadRange), first.getRanges());

		// Read the segments of the first buffer
		for (Range<Long> fread : freads) {
			ContiguousSet<Long> tmp = ContiguousSet.create(fread, DiscreteDomain.longs());
			long readStart = tmp.first();
			int tgtPos = Ints.checkedCast(readStart - srcOffset);
			int l = tmp.size();
			
			first.readInto(tgt, tgtOffset + tgtPos, readStart, l);			
		}

		// Read the segments of the second buffer
		for (Range<Long> sread : sreads) {
			ContiguousSet<Long> tmp = ContiguousSet.create(sread, DiscreteDomain.longs());
			long readStart = tmp.first();
			int tgtPos = Ints.checkedCast(readStart - srcOffset);
			int l = tmp.size();
			
			second.readInto(tgt, tgtOffset + tgtPos, readStart, l);			
		}

		return length;
	}

	@Override
	public Object get(long index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getCapacity() {
		return first.getCapacity();
	}

	@Override
	public RangeSet<Long> getRanges() {
		return unionRanges;
	}

	@Override
	public Long getOffsetInRanges() {
		long f = first.getOffsetInRanges();
		long s = second.getOffsetInRanges();
		
		Long result = f == s ? f : null;
		
		return result;
	}
	
	@Override
	public String toString() {
		return getRanges().toString();
	}
}

