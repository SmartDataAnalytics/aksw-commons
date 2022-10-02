package org.aksw.commons.io.util.channel;

import java.nio.channels.Channel;

public interface ChannelFactory<T extends Channel>
    extends AutoCloseable
{
    T newChannel();
}