package org.aksw.commons.io.input;

import java.io.IOException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.commons.io.buffer.array.ArrayOps;

public class ReadableChannelSourceOverStreamFactory<T>
    implements ReadableChannelSource<T[]>
{
    protected ArrayOps<T[]> arrayOps;
    protected Supplier<Stream<T>> streamFactory;

    public ReadableChannelSourceOverStreamFactory(ArrayOps<T[]> arrayOps, Supplier<Stream<T>> streamFactory) {
        super();
        this.arrayOps = arrayOps;
        this.streamFactory = streamFactory;
    }

    @Override
    public ReadableChannel<T[]> newReadableChannel(long offset, long end) throws IOException {
        Stream<T> stream = streamFactory.get();
        stream = stream.skip(offset);

        if (end != Long.MAX_VALUE) {
            long delta = end - offset;
            stream = stream.limit(delta);
        }

        return ReadableChannels.wrap(stream, arrayOps);
    }

    @Override
    public ArrayOps<T[]> getArrayOps() {
        return arrayOps;
    }

    @Override
    public long size() throws IOException {
        return -1;
    }
}
