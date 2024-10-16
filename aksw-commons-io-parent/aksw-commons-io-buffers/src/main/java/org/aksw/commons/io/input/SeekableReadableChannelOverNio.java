package org.aksw.commons.io.input;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

//public class SeekableReadableChannelOverNio {
public class SeekableReadableChannelOverNio<T extends SeekableByteChannel>
    extends ReadableChannelOverNio<T>
    implements SeekableReadableChannel<byte[]>
{
    public SeekableReadableChannelOverNio(T channel) {
        super(channel);
    }

    @Override
    public long position() throws IOException {
        return getDelegate().position();
    }

    @Override
    public void position(long pos) throws IOException {
        getDelegate().position(pos);
    }

    @Override
    public SeekableReadableChannel<byte[]> cloneObject() {
        throw new UnsupportedOperationException();
    }
}

