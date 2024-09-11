package org.aksw.commons.io.input;

import java.io.IOException;
import java.util.Objects;

import org.aksw.commons.io.buffer.array.ArrayOps;

public class SeekableReadableSourceWrapperWithMonitor<A>
    implements SeekableReadableChannelSource<A>
{
    protected SeekableReadableChannelSource<A> delegate;
    protected ChannelMonitor channelMonitor;

    public SeekableReadableSourceWrapperWithMonitor(SeekableReadableChannelSource<A> delegate) {
        this(delegate, new ChannelMonitor());
    }

    public SeekableReadableSourceWrapperWithMonitor(SeekableReadableChannelSource<A> delegate,
            ChannelMonitor channelMonitor) {
        super();
        this.delegate = Objects.requireNonNull(delegate);
        this.channelMonitor = Objects.requireNonNull(channelMonitor);
    }

    public SeekableReadableChannelSource<A> getDelegate() {
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
        return wrap(getDelegate().newReadableChannel(start));
    }

//    @Override
//    public SeekableReadableChannel<A> newReadableChannel(long start, long end) throws IOException {
//        return wrap(getDelegate().newReadableChannel(start, end));
//    }

    protected SeekableReadableChannel<A> wrap(SeekableReadableChannel<A> delegate) {
        return new SeekableReadableChannelWithMonitor<>(delegate, channelMonitor);
    }
}
