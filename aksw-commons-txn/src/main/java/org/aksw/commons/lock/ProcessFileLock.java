package org.aksw.commons.lock;

import java.nio.file.Path;
import java.util.concurrent.locks.Condition;

import org.aksw.commons.lock.db.impl.LockFromFile;

public class ProcessFileLock
    extends LockBaseRepeat
{
    // protected Path path;
    protected LockFromFile lock;

    // The thread that owns the lock (if any)
    protected transient Thread thread;


    // public ProcessFileLock(LockManager<Path> lockManager, Path relPath) {
    public ProcessFileLock(Path path) {
        super();
        this.lock = new LockFromFile(path);
        this.thread = null;
    }

//	@Override
//	public void lock() {
//		tryLock(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
//	}

    /**
     * First, attempt to create the process lock file.
     * If the manager already owns it then this step succeeds immediately without further waiting.
     *
     * Afterwards, attempt to get the thread lock
     *
     */
    @Override
    protected boolean singleLockAttempt() throws InterruptedException {
        Thread currentThread = Thread.currentThread();

        boolean result;
        if (thread == null) {
            result = lock.singleLockAttempt(); // LockManagerPath.tryCreateLockFile(path, time, unit);
            thread = Thread.currentThread();
        } else if (thread == currentThread) {
            result = true;
        } else {
            throw new RuntimeException("Attempt to re-lock a lock instance from a different thread");
        }

        return result;
    }

//    @Override
//    public boolean tryLock(long time, TimeUnit unit) {
//        Thread currentThread = Thread.currentThread();
//
//        boolean result;
//        if (thread == null) {
//            result = LockManagerPath.tryCreateLockFile(path, time, unit);
//            thread = Thread.currentThread();
//        } else if (thread == currentThread) {
//            result = true;
//        } else {
//            throw new RuntimeException("Attempt to re-lock a lock instance from a different thread");
//        }
//
//        return result;
//    }

    @Override
    public void unlock() {
        if (thread != null) {
//            try {
//                Files.delete(path);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
            lock.unlock();
            thread = null;
        }
    }


    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

//    @Override
//    public boolean tryLock() {
//        throw new UnsupportedOperationException();
//    }


    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }
}
