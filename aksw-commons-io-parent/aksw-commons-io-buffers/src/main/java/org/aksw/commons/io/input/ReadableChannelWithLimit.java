package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;

import com.google.common.primitives.Ints;

public class ReadableChannelWithLimit<A, X extends ReadableChannel<A>>
    implements ReadableChannel<A>
{
    protected X delegate;
    protected long limit;
    protected long remaining;

    public ReadableChannelWithLimit(X backend, long limit) {
        super();
        this.delegate = backend;
        this.limit = limit;
        this.remaining = limit;
    }

    public X getDelegate() {
        return delegate;
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return getDelegate().getArrayOps();
    }

    @Override
    public void close() throws IOException {
        getDelegate().close();
    }

    @Override
    public boolean isOpen() {
        return getDelegate().isOpen();
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        int result;
        if (remaining <= 0) {
            result = -1;
        } else {
            int n = Math.min(Ints.saturatedCast(remaining), length);
            result = getDelegate().read(array, position, n);

            if (result > 0) {
                remaining -= result;
            }
        }

        return result;
    }
}
