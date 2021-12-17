package org.aksw.commons.lock;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockUtils {
    private static final Logger logger = LoggerFactory.getLogger(LockUtils.class);

    /**
     * Perform an action which requires acquisition of a lock first.
     * Repeated attempts are made to acquire the lock. If the lock cannot be acquired then the action
     * is not run.
     *
     *
     * @param <T>
     * @param retryCount
     * @param delayInMs
     * @param lockSupplier
     * @param action
     * @return
     */
    public static <T> T repeatWithLock(
            int retryCount,
            int delayInMs,
            Lock lockSupplier,
            Callable<T> action) {
        T result = RetryUtils.simpleRetry(retryCount, delayInMs, () -> runWithLock(lockSupplier, action));
        return result;
    }

    /**
     * Perform an action which requires acquisition of a lock first.
     * An attempt is made to acquire the lock. If this fails then the action is not run.
     * Upon completion of the action (successful or exceptional) the lock is released again.
     *
     */
    public static <T> T runWithLock(Lock lock, Callable<T> action) {
        T result = null;
        try {
            lock.lock();
            result = action.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        return result;
    }

    public static void runWithLock(Lock lock, Runnable action) {
        runWithLock(lock, () -> { action.run(); return null; });
    }

    /**
     * Run this action with a short-lived locked. If the lock cannot be acquired
     * within the given time it is considered stale and forcibly unlocked.
     * Subsequently another attempt is made to acquire the lock.
     */
    public static <T, L extends Lock> T runWithMgmtLock(
            L lock,
            Consumer<? super L> forceUnlock,
            Duration duration,
            Callable<T> action) {
        T result = null;
        try {
            long timeout = duration.toMillis();
            boolean isLocked;
            if (!(isLocked = lock.tryLock(timeout, TimeUnit.MILLISECONDS))) {

                logger.warn(String.format("Forcibly unlocking stale lock %s", lock));
                forceUnlock.accept(lock);

                isLocked = lock.tryLock(timeout, TimeUnit.MILLISECONDS);
                if (!isLocked) {
                    throw new RuntimeException("Failed to acquire lock despite forced unlocking");
                }
            }

            result = action.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        return result;
    }

}
