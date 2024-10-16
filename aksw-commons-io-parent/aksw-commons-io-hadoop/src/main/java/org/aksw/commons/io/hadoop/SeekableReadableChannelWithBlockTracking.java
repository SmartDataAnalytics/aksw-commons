package org.aksw.commons.io.hadoop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.NavigableMap;

import org.aksw.commons.io.util.channel.SeekableByteChannelDecorator;

public class SeekableReadableChannelWithBlockTracking<T extends SeekableByteChannel>
    implements SeekableByteChannelDecorator
{
    protected T delegate;
    protected int endOfBlockMarker;
    protected long currentBlock = -1;
    protected NavigableMap<Long, Long> blockToSuccessor;

    public SeekableReadableChannelWithBlockTracking(T delegate) {
        this(delegate, -2);
    }

    public SeekableReadableChannelWithBlockTracking(T delegate, int endOfBlockMarker) {
        this.delegate = delegate;
        this.endOfBlockMarker = endOfBlockMarker;
    }

    @Override
    public SeekableByteChannel getDecoratee() {
        return delegate;
    }

    @Override
    public int read(ByteBuffer byteBuffer) throws IOException {
        int result = delegate.read(byteBuffer);
        if (result == endOfBlockMarker) {
            long pos = delegate.position();

            if (currentBlock != -1) {
                blockToSuccessor.put(currentBlock, pos);
                currentBlock = pos;
            }

            result = delegate.read(byteBuffer);
            if (result == endOfBlockMarker) {
                throw new IllegalStateException("Encountered consecutive end-of-block markers (with no data in between).");
            }
        }

        return result;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        Long blockStart = blockToSuccessor.floorKey(newPosition);
        if (blockStart == null) {
            currentBlock = -1;
            delegate.position(newPosition);
        } else {
            currentBlock = blockStart;
            delegate.position(blockStart);
        }
        return this;
    }
}
