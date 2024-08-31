package org.aksw.commons.io.input;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import org.aksw.commons.io.buffer.array.ArrayOps;

public class ReadableChannelOverNio<T extends ReadableByteChannel>
    implements ReadableChannel<byte[]>
{
    protected T delegate;

    public ReadableChannelOverNio(T channel) {
        super();
        this.delegate = channel;
    }

    public T getDelegate() {
        return delegate;
    }

    @Override
    public ArrayOps<byte[]> getArrayOps() {
        return ArrayOps.BYTE;
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
    public int read(byte[] array, int position, int length) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(array, position, length);
        int result = getDelegate().read(buf);
        return result;
    }
}
