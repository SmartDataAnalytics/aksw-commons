package org.aksw.commons.io.shared;

import java.nio.channels.Channel;

import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;

public abstract class ChannelBase
    extends AutoCloseableWithLeakDetectionBase
    implements Channel
{
    public ChannelBase() {
        this(true);
    }

    public ChannelBase(boolean enableInitializationStackTrace) {
        super(enableInitializationStackTrace);
    }

    @Override
    public boolean isOpen() {
        return !isClosed;
    }
}
