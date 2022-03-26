package org.aksw.commons.io.input;

import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;

public abstract class DataStreamBase<A>
    extends AutoCloseableWithLeakDetectionBase
    implements DataStream<A>
{
    @Override
    public boolean isOpen() {
        return !isClosed;
    }
}
