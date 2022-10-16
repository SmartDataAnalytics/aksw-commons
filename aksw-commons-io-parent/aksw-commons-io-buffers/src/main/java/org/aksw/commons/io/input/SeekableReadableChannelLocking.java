package org.aksw.commons.io.input;

public class SeekableReadableChannelLocking<A, X extends SeekableReadableChannel<A>>
    extends ReadableChannelLocking<A, X>
{
    public SeekableReadableChannelLocking(X delegate) {
        super(delegate);
    }
}
