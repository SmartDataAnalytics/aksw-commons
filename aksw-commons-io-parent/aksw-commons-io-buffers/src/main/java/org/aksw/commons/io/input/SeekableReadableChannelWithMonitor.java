package org.aksw.commons.io.input;

import java.io.IOException;
import java.util.Objects;

public class SeekableReadableChannelWithMonitor<A, X extends SeekableReadableChannel<A>>
    extends SeekableReadableChannelDecoratorBase<A, X>
{
    protected ChannelMonitor monitor;

    public SeekableReadableChannelWithMonitor(X delegate, ChannelMonitor monitor) {
        super(delegate);
        this.monitor = Objects.requireNonNull(monitor);
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        // Include positioning in the time so that long times may be discovered
        long startTimestamp = System.nanoTime();
        long startPos = super.position();
        int result = super.read(array, position, length);
        long endPos = super.position();
        long endTimestamp = System.nanoTime();
        long duration = endTimestamp - startTimestamp;
        monitor.submitReadStats(startPos, endPos, result, duration);

        return result;
    }

    @Override
    public void position(long pos) throws IOException {
        super.position(pos);
    }
}
