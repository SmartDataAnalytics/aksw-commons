package net.sansa_stack.nio.util;

import java.nio.channels.WritableByteChannel;

public class WritableByteChannelWrapperBase<T extends WritableByteChannel>
    implements WritableByteChannelWrapper
{
    protected T delegate;

    public WritableByteChannelWrapperBase(T delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public T getDelegate() {
        return delegate;
    }
}
