package org.aksw.commons.lock;

import java.util.concurrent.TimeUnit;

import org.aksw.commons.util.retry.RetryUtils;

public abstract class LockBaseRepeat
    extends LockBase
{
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return tryLockActual(time, unit);
    }

    protected boolean tryLockActual(long time, TimeUnit unit) throws InterruptedException {
        long ms = unit.toMillis(time);
        long retryIntervalInMs = 100;
        long retryCount = (ms / retryIntervalInMs) + (ms % retryIntervalInMs == 0 ? 0 : 1);

        boolean result = RetryUtils.simpleRetry(retryCount, retryIntervalInMs, () -> {
            boolean r = singleLockAttempt();
            return r;
        });

        return result;
    }

    // protected boolean runLockAttempt() { return false; }
    protected abstract boolean singleLockAttempt() throws InterruptedException;
}
