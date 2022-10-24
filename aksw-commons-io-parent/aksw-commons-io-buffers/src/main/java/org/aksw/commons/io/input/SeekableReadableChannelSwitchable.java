package org.aksw.commons.io.input;

public class SeekableReadableChannelSwitchable<A>
    extends ReadableChannelSwitchableBase<A, SeekableReadableChannel<A>>
    implements SeekableReadableChannel<A>
{
    public SeekableReadableChannelSwitchable(SeekableReadableChannel<A> delegate) {
        super(delegate);
    }

    @Override
    public long position() {
        return decoratee.position();
    }

    @Override
    public void position(long pos) {
        decoratee.position(pos);
    }

    @Override
    public SeekableReadableChannel<A> cloneObject() {
        return new SeekableReadableChannelSwitchable<>(decoratee.cloneObject());
    }
}
