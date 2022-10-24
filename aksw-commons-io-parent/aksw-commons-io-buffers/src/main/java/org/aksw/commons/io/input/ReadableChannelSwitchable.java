package org.aksw.commons.io.input;

/** Decorator meant to switch from a buffered stream to a non-buffering one */
public class ReadableChannelSwitchable<A>
    extends ReadableChannelSwitchableBase<A, ReadableChannel<A>>
{
    public ReadableChannelSwitchable(ReadableChannel<A> delegate) {
        super(delegate);
    }
}
