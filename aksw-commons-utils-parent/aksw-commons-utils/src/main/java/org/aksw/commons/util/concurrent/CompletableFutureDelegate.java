package org.aksw.commons.util.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * CompletableFuture with a delegate that can be set at most once.
 * If this future was cancelled before the delegate was set, then the delegate
 * will be cancelled immediately when it is set.
 *
 * @author raven
 *
 * @param <T>
 */
public class CompletableFutureDelegate<T>
    extends CompletableFuture<T>
{
    protected Future<?> delegate;
    protected Boolean isCancelled = null; // non-null means cancelled; the value is the argument to the cancel method

    public CompletableFutureDelegate() {
        this(null);
    }

    public CompletableFutureDelegate(Future<?> delegate) {
        super();
        this.delegate = delegate;
    }

    public void setDelegate(Future<?> delegate) {
        if (this.delegate != null) {
            throw new IllegalStateException("Delegate has already been set");
        }

        this.delegate = delegate;

        if (isCancelled != null) {
            cancel(isCancelled);
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        isCancelled = mayInterruptIfRunning;

        boolean result;
        if (delegate != null) {
            result = this.delegate.cancel(mayInterruptIfRunning);
            super.cancel(mayInterruptIfRunning);
        } else {
            // The delegate has not been set yet so we don't actually know whether
            // once it is set its cancel method will return true ; we assue it does
            result = true;
        }

        return result;
    }


}
