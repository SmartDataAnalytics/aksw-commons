package org.aksw.commons.util.closeable;

import org.aksw.commons.util.stack_trace.StackTraceUtils;

/** Object base class that supports tracking the stack trace of its construction */
public class ObjectBase {
    protected final StackTraceElement[] instantiationStackTrace;

    public ObjectBase() {
        this(false);
    }

    public ObjectBase(boolean allowTrackInstantiation) {
        instantiationStackTrace = allowTrackInstantiation
                ? StackTraceUtils.getStackTraceIfEnabled()
                : null;
    }

    public StackTraceElement[] getInstantiationStackTrace() {
        return instantiationStackTrace;
    }
}
