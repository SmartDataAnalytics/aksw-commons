package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;

import com.google.common.collect.Range;

public abstract class SeekableReadableSourceWrapperBase<A, X extends SeekableReadableChannelSource<A>>
    implements SeekableReadableChannelSource<A>
{
    protected X delegate;

    public SeekableReadableSourceWrapperBase(X delegate) {
        super();
        this.delegate = delegate;
    }

    public X getDelegate() {
        return delegate;
    }

    @Override
    public long size() throws IOException {
        return getDelegate().size();
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return getDelegate().getArrayOps();
    }

    @Override
    public SeekableReadableChannel<A> newReadableChannel() throws IOException {
        return wrap(getDelegate().newReadableChannel());
    }

    @Override
    public SeekableReadableChannel<A> newReadableChannel(long start) throws IOException {
        SeekableReadableChannel<A> result = newReadableChannel();
        result.position(start);
        return result;
        // return wrap(getDelegate().newReadableChannel(start));
    }

    @Override
    public SeekableReadableChannel<A> newReadableChannel(long start, long end) throws IOException {
        SeekableReadableChannel<A> result = newReadableChannel();
        result.position(start);
        return result;
        // return wrap(getDelegate().newReadableChannel(start, end));
    }

    @Override
    public SeekableReadableChannel<A> newReadableChannel(Range<Long> range) throws IOException {
        throw new UnsupportedOperationException();
//        SeekableReadableChannel<A> result = newReadableChannel();
//        result.position(range.low);
//        return result;
        // return wrap(getDelegate().newReadableChannel(range));
    }

    protected abstract SeekableReadableChannel<A> wrap(SeekableReadableChannel<A> delegate);
}
