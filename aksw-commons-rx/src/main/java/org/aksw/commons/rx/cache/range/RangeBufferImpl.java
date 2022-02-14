package org.aksw.commons.rx.cache.range;

import java.io.IOException;

import org.aksw.commons.util.array.ArrayOps;
import org.aksw.commons.util.array.Buffer;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.google.common.math.LongMath;


public class RangeBufferImpl<A>
	implements RangeBuffer<A>
{
	// The offset of this buffer in the set of covered ranges; i.e. writing to position 0 of this buffer may actually
	// correspond to a range starting at offset 100
	protected RangeSet<Long> ranges;
	protected long offsetInRanges;
	protected Buffer<A> buffer;
	
	public RangeBufferImpl(RangeSet<Long> ranges, long offsetInRanges, Buffer<A> buffer) {
		super();
		this.ranges = ranges;
		this.offsetInRanges = offsetInRanges;
		this.buffer = buffer;
	}

	public static <A> RangeBufferImpl<A> create(RangeSet<Long> ranges, long offsetInRanges, Buffer<A> buffer) {
		return new RangeBufferImpl<>(ranges, offsetInRanges, buffer);
	}

	public static <A> RangeBufferImpl<A> create(Buffer<A> buffer) {
		return create(TreeRangeSet.create(), 0, buffer);
	}

	public static <A> RangeBufferImpl<A> wrap(Buffer<A> buffer) {
		Range<Long> range = Range.closedOpen(0l, (long)buffer.getCapacity());
		RangeSet<Long> rangeSet = TreeRangeSet.create();
		rangeSet.add(range);
		return create(rangeSet, 0, buffer);
	}

	
	public RangeSet<Long> getRanges() {
		return ranges;
	}
	
	@Override
	public long getCapacity() {
		return buffer.getCapacity();
	}

	@Override
	public Long getOffsetInRanges() {
		return offsetInRanges;
	}
	
	@Override
	public ArrayOps<A> getArrayOps() {
		return buffer.getArrayOps();
	}

	/**
	 * Read operation that blocks when attempting to access uncovered regions.
	 * @throws IOException 
	 * 
	 */
	@Override
	public int readInto(A tgt, int tgtOffset, long srcOffset, int length) throws IOException {
		
		long start = srcOffset + offsetInRanges;
		long end = start + length;		
		Range<Long> totalReadRange = Range.closedOpen(start, end);
		
		if (!ranges.encloses(totalReadRange)) {
			RangeSet<Long> gaps = ranges.complement().subRangeSet(totalReadRange);
			
			throw new ReadOverGapException("Attempt to read over gaps at: " + gaps);
		}

		int result = buffer.readInto(tgt, tgtOffset, srcOffset, length);
		return result;
	}

	@Override
	public Object get(long index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(long offsetInBuffer, Object arrayWithItemsOfTypeT, int arrOffset, int arrLength) throws IOException {		
		long start = LongMath.checkedAdd(offsetInRanges, offsetInBuffer);
		long end = LongMath.checkedAdd(start, arrLength);

		if (end >= buffer.getCapacity()) {
			throw new RuntimeException("Attempt to write beyond buffer capacity");
		}

		// Range<Long> writeRange = Range.closedOpen(start, end);
		
		// TODO Add debug mode: Check when writing to already known ranges
		
		buffer.putAll(offsetInBuffer, arrayWithItemsOfTypeT, arrOffset, arrLength);
		ranges.add(Range.closedOpen(start, end));
	}

	@Override
	public void put(long offset, Object item) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString() {
		return getRanges().toString();
	}
}
