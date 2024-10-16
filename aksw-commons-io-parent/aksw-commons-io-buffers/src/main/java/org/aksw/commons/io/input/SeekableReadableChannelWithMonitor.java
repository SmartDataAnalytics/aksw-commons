package org.aksw.commons.io.input;

import java.io.IOException;
import java.util.Objects;

public class SeekableReadableChannelWithMonitor<A, X extends SeekableReadableChannel<A>>
    extends SeekableReadableChannelDecoratorBase<A, X>
{
    protected ChannelMonitor2 monitor;

    protected long cachedPos;
    protected long relativeStart;
    protected long readLength;

    protected volatile long readCounter = 0;

    public SeekableReadableChannelWithMonitor(X delegate, ChannelMonitor2 monitor) {
        super(delegate);
        this.monitor = Objects.requireNonNull(monitor);
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        // Include positioning in the time so that long times may be discovered
        long startTimestamp = System.nanoTime();
        int result = super.read(array, position, length);
        if (result > 0) {
            // long endPos = super.position();
            long endTimestamp = System.nanoTime();
            long duration = endTimestamp - startTimestamp;
            long nextStart = relativeStart + result;
            // System.out.println(String.format("Read #%d: pos %d len: %d", ++readCounter, cachedPos, result)) ;
            monitor.incReadCounter();
            monitor.addReadAmount(result);
            // monitor.submitReadStats(cachedPos, relativeStart, nextStart, result, duration);
            relativeStart = nextStart;
        }
        return result;
    }

    @Override
    public void position(long pos) throws IOException {
        this.cachedPos = pos;
        super.position(pos);
    }
}
