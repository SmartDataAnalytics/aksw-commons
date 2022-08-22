package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.shared.ChannelDecoratorBase;

public class ReadableChannelDecoratorBase<A, X extends ReadableChannel<A>>
    extends ChannelDecoratorBase<X>
    implements ReadableChannel<A>
{
    public ReadableChannelDecoratorBase(X delegate) {
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
}
