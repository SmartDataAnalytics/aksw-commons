package org.aksw.commons.io.input;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class SeekableByteChannelAdapter<T extends SeekableReadableChannel<byte[]>>
    extends ReadableByteChannelAdapter<T>
    implements SeekableByteChannel
{
    public SeekableByteChannelAdapter(T delegate) {
        super(delegate);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long position() throws IOException {
        return getDelegate().position();
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        getDelegate().position(newPosition);
        return this;
    }

    @Override
    public long size() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        throw new UnsupportedOperationException();
    }
}
