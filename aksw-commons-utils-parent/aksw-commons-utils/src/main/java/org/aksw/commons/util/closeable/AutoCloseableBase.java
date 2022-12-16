package org.aksw.commons.util.closeable;

import org.aksw.commons.util.stack_trace.StackTraceUtils;

public class AutoCloseableBase
    implements AutoCloseable
{
    protected volatile boolean isClosed = false;

    protected boolean enableCloseStackTrace;
    protected StackTraceElement[] closeStackTrace = null;

    public AutoCloseableBase() {
        this(true);
    }

    public AutoCloseableBase(boolean enableCloseStackTrace) {
        this.enableCloseStackTrace = enableCloseStackTrace;
    }

    /**
     * To be called within synchronized functions
     */
    protected void ensureOpen() {
        if (isClosed) {
            String str = StackTraceUtils.toString(closeStackTrace);
            throwClosedException("Object already closed at: " + str);
        }
    }

    protected void throwClosedException(String msg) {
        throw new RuntimeException(msg);
    }

    protected void closeActual() throws Exception { /* nothing to do */ }

    @Override
    public final void close() {
        if (!isClosed) {
            synchronized (this) {
                if (!isClosed) {
                    closeStackTrace = enableCloseStackTrace ? StackTraceUtils.getStackTraceIfEnabled() : null;
                    isClosed = true;

                    try {
                        closeActual();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
