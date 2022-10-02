package org.aksw.commons.io.hadoop.binseach.bz2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.NavigableMap;

import net.sansa_stack.nio.util.SeekableByteChannelDecorator;

public class SeekableReadableChannelWithBlockTracking<T extends SeekableByteChannel>
    implements SeekableByteChannelDecorator
    // implements SeekableByteChannel
{
    protected T delegate;
    protected int endOfBlockMarker;
    protected long currentBlock = -1;
    protected NavigableMap<Long, Long> blockToSuccessor;

    public SeekableReadableChannelWithBlockTracking(T delegate, int endOfBlockMarker) {
        // super(delegate);
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
