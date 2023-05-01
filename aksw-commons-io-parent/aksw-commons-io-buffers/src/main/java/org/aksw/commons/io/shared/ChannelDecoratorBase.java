package org.aksw.commons.io.shared;

import java.io.IOException;
import java.nio.channels.Channel;

import org.aksw.commons.util.closeable.AutoCloseableDecoratorBase;

public class ChannelDecoratorBase<T extends Channel>
    extends AutoCloseableDecoratorBase<T>
    implements Channel
{
    public ChannelDecoratorBase(T decoratee) {
        super(decoratee);
    }

    @Override
    public boolean isOpen() {
        return decoratee.isOpen();
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
