package org.aksw.commons.rx.cache.range;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

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

    protected final StackTraceElement[] instantiationStackTrace = Thread.currentThread().getStackTrace();

    public StackTraceElement[] getInstantiationStackTrace() {
        return instantiationStackTrace;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (!isClosed) {
                String str = instantiationStackTrace == null
                        ? "(no stack trace available)"
                        : Arrays.asList(instantiationStackTrace).stream().map(s -> "  " + Objects.toString(s))
                            .collect(Collectors.joining("\n"));

                logger.warn("Ref released by GC rather than user logic - indicates resource leak. Acquired at " + str);

                close();
            }
        } finally {
            super.finalize();
        }
    }
}
