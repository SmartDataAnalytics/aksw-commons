package org.aksw.commons.io.input;

import org.aksw.commons.io.shared.ChannelBase;

public abstract class ReadableChannelBase<A>
    extends ChannelBase
    implements ReadableChannel<A>
{
//    @Override
//    protected void throwClosedException(String msg) {
//        throw new ClosedChannelException(msg);
//    }
}
