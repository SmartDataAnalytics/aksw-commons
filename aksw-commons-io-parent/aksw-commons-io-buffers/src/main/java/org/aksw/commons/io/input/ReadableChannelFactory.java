package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.HasArrayOps;

import com.google.common.collect.Range;

public interface ReadableChannelFactory<A>
    extends HasArrayOps<A>
{
    default ReadableChannel<A> newReadableChannel() throws IOException {
        return newReadableChannel(0l);
    }

    default ReadableChannel<A> newReadableChannel(long offset) throws IOException {
        return newReadableChannel(Range.atLeast(offset));
    }

    /** Offsets typically start with 0 but the interface contract leaves that unspecified */
    default ReadableChannel<A> newReadableChannel(Range<Long> range) throws IOException {
        long lowerEndpoint = range.hasLowerBound() ? range.lowerEndpoint() : Long.MIN_VALUE;
        long upperEndpoint = range.hasUpperBound() ? range.upperEndpoint() : Long.MAX_VALUE;
        return newReadableChannel(lowerEndpoint, upperEndpoint);
    }

    ReadableChannel<A> newReadableChannel(long start, long end) throws IOException;
}
