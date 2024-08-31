package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;

/**
 * The channel adds the open/close mechanic.
 *
 * @param <A>
 */
public class ReadableChannelOverReadableSource<A>
    extends ReadableChannelBase<A>
{
    protected ReadableSource<A> source;

    public ReadableChannelOverReadableSource(ReadableSource<A> source) {
        super();
        this.source = source;
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        return source.read(array, position, length);
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return source.getArrayOps();
    }
}
