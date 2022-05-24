package org.aksw.commons.io.input;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import org.aksw.commons.io.buffer.array.ArrayOps;

public class ReadableChannelOverReadableByteChannel
    implements ReadableChannel<byte[]>
{
    protected ReadableByteChannel channel;

    public ReadableChannelOverReadableByteChannel(ReadableByteChannel channel) {
        super();
        this.channel = channel;
    }

    @Override
    public ArrayOps<byte[]> getArrayOps() {
        return ArrayOps.BYTE;
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public int read(byte[] array, int position, int length) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(array, position, length);
        int result = channel.read(buf);
        return result;
    }

}
