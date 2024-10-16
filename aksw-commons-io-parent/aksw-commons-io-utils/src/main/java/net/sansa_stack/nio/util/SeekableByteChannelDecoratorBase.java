package net.sansa_stack.nio.util;

import java.nio.channels.SeekableByteChannel;

import org.aksw.commons.io.util.channel.SeekableByteChannelDecorator;

public class SeekableByteChannelDecoratorBase<T extends SeekableByteChannel>
    implements SeekableByteChannelDecorator
{
    protected T decoratee;

    public SeekableByteChannelDecoratorBase(T decoratee) {
        super();
        this.decoratee = decoratee;
    }

    @Override
    public T getDecoratee() {
        return decoratee;
    }
}
