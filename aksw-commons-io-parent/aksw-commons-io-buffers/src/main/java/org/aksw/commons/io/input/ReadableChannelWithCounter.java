package org.aksw.commons.io.input;

import java.io.IOException;

public class ReadableChannelWithCounter<A, X extends ReadableChannel<A>>
    extends ReadableChannelDecoratorBase<A, X>
{
    protected long count = 0;

    public ReadableChannelWithCounter(X delegate) {
        super(delegate);
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        int n = decoratee.read(array, position, length);
        if (n > 0) {
            ++count;
        }
        return n;
    }

    public long getReadCount() {
        return count;
    }

    public X getDecoratee() {
        return decoratee;
    }
}
