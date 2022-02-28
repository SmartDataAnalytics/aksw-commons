package org.aksw.commons.rx.cache.range;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.commons.util.array.ArrayOps;
import org.aksw.commons.util.array.Buffer;

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
    public void write(long offsetInBuffer, A arrayWithItemsOfTypeT, int arrOffset, int arrLength) throws IOException {
        first.write(offsetInBuffer, arrayWithItemsOfTypeT, arrOffset, arrLength);
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
    public RangeSet<Long> getCoveredRanges(Range<Long> localRange) {
        RangeSet<Long> firstCovers = first.getCoveredRanges(localRange);
        RangeSet<Long> secondCovers = second.getCoveredRanges(localRange);
        RangeSet<Long> result = RangeSetOps.union(firstCovers, secondCovers);
        return result;
    }

//    @Override
//    public RangeSet<Long> getAvailableGlobalRanges(Range<Long> bufferRange) {
//        // TODO Auto-generated method stub
//        return null;
//    }

    @Override
    public int readInto(A tgt, int tgtOffset, long srcOffset, int length) throws IOException {
        long end = srcOffset + length;
        Range<Long> totalLocalReadRange = Range.closedOpen(srcOffset, end);

        RangeSet<Long> gaps = TreeRangeSet.create();
        gaps.add(totalLocalReadRange);

        List<RangeBuffer<A>> buffers = Arrays.asList(first, second);

        // For each buffer the range set of corresponding reads
        List<RangeSet<Long>> readSchedules = new ArrayList<>();
        for (int i = 0; i < buffers.size() && !gaps.isEmpty(); ++i) {
            RangeSet<Long> schedule = TreeRangeSet.create();
            readSchedules.add(schedule);
            RangeBuffer<A> buffer = buffers.get(i);

            for (Range<Long> gap : gaps.asRanges()) {
                RangeSet<Long> contribs = buffer.getCoveredRanges(gap);
                gaps.removeAll(contribs);

                schedule.addAll(contribs);
            }
        }

        if (!gaps.isEmpty()) {
            throw new RuntimeException("Attempt to read over gaps at: " + gaps);
        }

        for (int i = 0; i < readSchedules.size(); ++i) {
            RangeBuffer<A> buffer = buffers.get(i);
            RangeSet<Long> schedule = readSchedules.get(i);

            for (Range<Long> range : schedule.asRanges()) {
                ContiguousSet<Long> tmp = ContiguousSet.create(range, DiscreteDomain.longs());
                long readStart = tmp.first();
                int tgtPos = Ints.checkedCast(readStart - srcOffset);
                int l = tmp.size();

                int tgtStart = tgtOffset + tgtPos;
                buffer.readInto(tgt, tgtStart, readStart, l);

                if (first.getArrayOps().get(tgt, tgtStart) == null) {
                    throw new NullPointerException("Unexpected null read");
                }
            }
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

    @Override
    public Buffer<A> getBackingBuffer() {
        return null;
    }
}


// old approach that did not create explicit scheduler for each union member - now approach scales to n-ary unions
//
//long firstOffset = first.getOffsetInRanges();
//Range<Long> firstReadRange = Range.closedOpen(firstOffset + srcOffset, firstOffset + length);
//
//Set<Range<Long>> freads = first.getRanges().subRangeSet(firstReadRange).asRanges();
//Set<Range<Long>> sreads = RangeSetUtils.difference(second.getRanges().subRangeSet(totalReadRange), first.getRanges());
//
//// Read the segments of the first buffer
//for (Range<Long> fread : freads) {
//  ContiguousSet<Long> tmp = ContiguousSet.create(fread, DiscreteDomain.longs());
//  long readStart = tmp.first();
//  int tgtPos = Ints.checkedCast(readStart - srcOffset);
//  int l = tmp.size();
//
//  first.readInto(tgt, tgtOffset + tgtPos, readStart, l);
//}
//
//// Read the segments of the second buffer
//for (Range<Long> sread : sreads) {
//  ContiguousSet<Long> tmp = ContiguousSet.create(sread, DiscreteDomain.longs());
//  long readStart = tmp.first();
//  int tgtPos = Ints.checkedCast(readStart - srcOffset);
//  int l = tmp.size();
//
//  second.readInto(tgt, tgtOffset + tgtPos, readStart, l);
//}

//if (!unionRanges.encloses(totalReadRange)) {
//// UnionRangeSet does not yet provide a .complement() view
//Set<Range<Long>> gaps = RangeSetUtils.complement(unionRanges, totalReadRange);
//// RangeSet<Long> gaps = unionRanges.complement().subRangeSet(totalReadRange);
//
//throw new RuntimeException("Attempt to read over gaps at: " + gaps);
//}

