package org.aksw.commons.io.input;

import java.io.IOException;

import com.google.common.collect.Range;

public interface SeekableReadableChannelSource<A>
    extends ReadableChannelSource<A>
{
    @Override
    SeekableReadableChannel<A> newReadableChannel() throws IOException;

    @Override
    default SeekableReadableChannel<A> newReadableChannel(long offset) throws IOException {
        SeekableReadableChannel<A> result = newReadableChannel();
        result.position(offset);
        return result;
    }

    @Override
    default SeekableReadableChannel<A> newReadableChannel(long start, long end) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    default SeekableReadableChannel<A> newReadableChannel(Range<Long> range) throws IOException {
        throw new UnsupportedOperationException();
    }


//        return newReadableChannel(0l);
//    }

    // Create a new channel positioned at the given offset
//    @Override
//    SeekableReadableChannel<A> newReadableChannel(long offset) throws IOException;
        // return newReadableChannel(Range.atLeast(offset));
    // }

    /** Offsets typically start with 0 but the interface contract leaves that unspecified */
//    @Deprecated // This creates a channel with a sub-range
//    @Override
//    default SeekableReadableChannel<A> newReadableChannel(Range<Long> range) throws IOException {
//        long lowerEndpoint = range.hasLowerBound() ? range.lowerEndpoint() : Long.MIN_VALUE;
//        long upperEndpoint = range.hasUpperBound() ? range.upperEndpoint() : Long.MAX_VALUE;
//        return newReadableChannel(lowerEndpoint, upperEndpoint);
//    }

//    @Deprecated // This creates a channel with a sub-range
//    @Override
//    SeekableReadableChannel<A> newReadableChannel(long start, long end) throws IOException;
}
