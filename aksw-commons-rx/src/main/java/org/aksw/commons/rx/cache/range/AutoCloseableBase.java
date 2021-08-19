package org.aksw.commons.rx.cache.range;

public class AutoCloseableBase
    implements AutoCloseable
{
    protected volatile boolean isClosed = false;

    /**
     * To be called within synchronized functions
     */
    protected void ensureOpen() {
        if (isClosed) {
            throw new RuntimeException("Object already closed");
        }
    }

    protected void closeActual() {}

    @Override
    public final synchronized void close() {
        if (!isClosed) {
            isClosed = true;
            closeActual();
        }
    }
}
