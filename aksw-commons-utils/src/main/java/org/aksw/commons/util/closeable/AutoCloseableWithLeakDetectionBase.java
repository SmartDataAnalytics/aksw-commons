package org.aksw.commons.util.closeable;

import org.aksw.commons.util.stack_trace.StackTraceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base implementation of AutoClosable that helps detecting resource leaks.
 * Creation of an instance of this class captures a snapshot of the stack trace.
 * If finalize is called (typically only by the GC) and there was no prior call to close then
 * a warning including the stack trace is logged.
 *
 * Implementing classes should override {@link #closeActual()} rather than
 * {@link #close()}.
 *
 * @author Claus Stadler
 *
 */
public class AutoCloseableWithLeakDetectionBase
    extends AutoCloseableBase
{
    private static final Logger logger = LoggerFactory.getLogger(AutoCloseableWithLeakDetectionBase.class);

    protected final StackTraceElement[] instantiationStackTrace;

    public AutoCloseableWithLeakDetectionBase() {
        this(true);
    }

    public AutoCloseableWithLeakDetectionBase(boolean enableInstantiationStackTrace) {
        this.instantiationStackTrace = enableInstantiationStackTrace
                ? StackTraceUtils.getStackTraceIfEnabled()
                : null;
    }

    public StackTraceElement[] getInstantiationStackTrace() {
        return instantiationStackTrace;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (!isClosed) {
                String str = StackTraceUtils.toString(instantiationStackTrace);

                logger.warn("Close invoked via GC rather than user logic - indicates resource leak. Object constructed at " + str);

                close();
            }
        } finally {
            super.finalize();
        }
    }
}
