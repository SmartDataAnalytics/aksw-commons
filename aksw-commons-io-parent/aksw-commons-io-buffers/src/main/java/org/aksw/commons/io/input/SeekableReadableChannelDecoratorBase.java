package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;

public class SeekableReadableChannelDecoratorBase<A, X extends SeekableReadableChannel<A>>
    extends ReadableChannelDecoratorBase<A, X>
    implements SeekableReadableChannel<A>
{
    public SeekableReadableChannelDecoratorBase(X delegate) {
        super(delegate);
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return decoratee.getArrayOps();
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        return decoratee.read(array, position, length);
    }

    @Override
    public long position() {
        return decoratee.position();
    }

    @Override
    public void position(long pos) {
        decoratee.position(pos);
    }

    @Override
    public SeekableReadableChannel<A> cloneObject() {
        throw new UnsupportedOperationException("clone not supported");
    }
}
