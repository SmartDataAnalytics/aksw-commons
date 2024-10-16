package org.aksw.commons.io.buffer.array;

import java.io.IOException;

import org.aksw.commons.io.input.SeekableReadableChannel;
import org.aksw.commons.io.input.SeekableReadableChannelSource;
import org.aksw.commons.io.input.SeekableReadableChannels;

public interface ArrayReadable<A>
    extends SeekableReadableChannelSource<A>
{
    int readInto(A tgt, int tgtOffset, long srcOffset, int length) throws IOException;

    // TODO start and end are bounds, but we use start also as the initial position - we shouldn't conflate these concepts
    @Override
    default SeekableReadableChannel<A> newReadableChannel() throws IOException {
        // Preconditions.checkArgument(start <= end, String.format("Start (%d) must be <= end (%d)", start, end));
        SeekableReadableChannel<A> result = SeekableReadableChannels.newChannel(this, 0);
//        if (Long.MAX_VALUE != end) {
//            long length = end - start;
//            result = ReadableChannels.ra(result, length);
//        }
        return result;
    }

    @SuppressWarnings("unchecked")
    default int readIntoRaw(Object tgt, int tgtOffset, long srcOffset, int length) throws IOException {
        return readInto((A)tgt, tgtOffset, srcOffset, length);
    }

    default Object get(long index) throws IOException {
        ArrayOps<A> arrayOps = getArrayOps();
        A singleton = arrayOps.create(1);
        readInto(singleton, 0, index, 1);
        Object result = arrayOps.get(singleton, 0);
        return result;
    }

    @Override
    default long size() throws IOException {
        return -1;
    }

    // T get(long index);
    // Iterator<T> iterator(long offset);
}
