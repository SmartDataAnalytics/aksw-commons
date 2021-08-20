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

    protected void closeActual() throws Exception {}

    @Override
    public final synchronized void close() {
        if (!isClosed) {
            isClosed = true;

            try {
                closeActual();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
