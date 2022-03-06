package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;

import com.google.common.primitives.Ints;

public class DataStreamLimit<A>
    implements DataStream<A>
{
    protected DataStream<A> delegate;
    protected long limit;
    protected long remaining;

    public DataStreamLimit(DataStream<A> backend, long limit) {
        super();
        this.delegate = backend;
        this.limit = limit;
        this.remaining = limit;
    }

    public DataStream<A> getDelegate() {
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
