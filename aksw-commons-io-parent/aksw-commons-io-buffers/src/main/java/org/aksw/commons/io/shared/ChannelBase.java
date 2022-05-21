package org.aksw.commons.io.shared;

import java.nio.channels.Channel;

import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;

public abstract class ChannelBase
	extends AutoCloseableWithLeakDetectionBase
	implements Channel
{
	@Override
	public boolean isOpen() {
	    return !isClosed;
	}
}
