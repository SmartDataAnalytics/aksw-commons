package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.shared.ChannelDecoratorBase;

public class SeekableReadableChannelWithOffset<A, X extends SeekableReadableChannel<A>>
    extends ChannelDecoratorBase<X>
    implements SeekableReadableChannel<A>
{
    protected long offset;

    public SeekableReadableChannelWithOffset(X delegate, long offset) {
        super(delegate);
        this.offset = offset;
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        return decoratee.read(array, position, length);
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return decoratee.getArrayOps();
    }

    @Override
    public boolean isOpen() {
        return decoratee.isOpen();
    }

    @Override
    public long position() {
        long physicalPos = decoratee.position();
        long result = physicalPos - offset;
        return result;
    }

    @Override
    public void position(long position) {
        long p = offset + position;
        decoratee.position(p);
    }

    @Override
    public SeekableReadableChannel<A> cloneObject() {
        return new SeekableReadableChannelWithOffset<>(decoratee.cloneObject(), offset);
    }
}
