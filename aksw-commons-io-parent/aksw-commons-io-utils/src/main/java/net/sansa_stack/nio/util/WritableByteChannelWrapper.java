package net.sansa_stack.nio.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public interface WritableByteChannelWrapper
    extends WritableByteChannel
{
    WritableByteChannel getDelegate();

    @Override
    default boolean isOpen() {
    return getDelegate().isOpen();
    }

    @Override
    default void close() throws IOException {
        getDelegate().close();
    }

    @Override
    default int write(ByteBuffer byteBuffer) throws IOException {
        return getDelegate().write(byteBuffer);
    }
}
