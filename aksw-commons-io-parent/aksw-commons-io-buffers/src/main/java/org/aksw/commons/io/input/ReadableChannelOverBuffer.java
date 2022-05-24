package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.buffer.plain.Buffer;

public class ReadableChannelOverBuffer<A>
    extends ReadableChannelBase<A>
{
    protected Buffer<A> buffer;
    protected long offset;

    public ReadableChannelOverBuffer(Buffer<A> buffer, long offset) {
        super();
        this.buffer = buffer;
        this.offset = offset;
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return buffer.getArrayOps();
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        int n = buffer.readInto(array, position, offset, length);
        if (n >= 0) {
            offset += n;
        }
        return n;
    }
}
