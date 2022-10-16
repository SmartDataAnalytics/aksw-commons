package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.shared.ChannelDecoratorBase;

public class SeekableReadableChannelWithLimit<A, X extends SeekableReadableChannel<A>>
    extends ChannelDecoratorBase<X>
    implements SeekableReadableChannel<A>
{
    protected long limit;

    public SeekableReadableChannelWithLimit(X delegate, long limit) {
        super(delegate);
        this.limit = limit;
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return decoratee.getArrayOps();
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
    public int read(A array, int position, int length) throws IOException {
        long pos = decoratee.position();
        int l = Math.max(0, (int)Math.min(limit - pos, length));
        int result = l == 0
                ? length == 0 ? 0 : -1
                : decoratee.read(array, position, l);
        return result;
    }

    @Override
    public SeekableReadableChannel<A> cloneObject() {
        return new SeekableReadableChannelWithLimit<>(decoratee.cloneObject(), limit);
    }
}
