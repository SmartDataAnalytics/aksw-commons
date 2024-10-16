package org.aksw.commons.io.input;

import java.util.Objects;

public class SeekableReadableSourceWithMonitor<A, X extends SeekableReadableChannelSource<A>>
    extends SeekableReadableSourceWrapperBase<A, X>
{
    protected ChannelMonitor2 channelMonitor;

    public SeekableReadableSourceWithMonitor(X delegate) {
        this(delegate, new ChannelMonitor2());
    }

    public ChannelMonitor2 getChannelMonitor() {
        return channelMonitor;
    }

    public SeekableReadableSourceWithMonitor(X delegate,
            ChannelMonitor2 channelMonitor) {
        super(Objects.requireNonNull(delegate));
        this.channelMonitor = Objects.requireNonNull(channelMonitor);
    }

    @Override
    protected SeekableReadableChannel<A> wrap(SeekableReadableChannel<A> delegate) {
        return new SeekableReadableChannelWithMonitor<>(delegate, channelMonitor);
    }
}
