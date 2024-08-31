package org.aksw.commons.io.util.channel;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

import net.sansa_stack.nio.util.SeekableByteChannelDecoratorBase;

public class SeekableByteChannelWithCloseShield
    extends SeekableByteChannelDecoratorBase<SeekableByteChannel>
{
    public SeekableByteChannelWithCloseShield(SeekableByteChannel decoratee) {
        super(decoratee);
    }

    @Override
    public void close() throws IOException {
        // no op
    }
}
