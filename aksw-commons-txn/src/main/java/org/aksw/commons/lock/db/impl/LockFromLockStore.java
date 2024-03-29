package org.aksw.commons.lock.db.impl;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Stream;

import org.aksw.commons.lock.LockBaseRepeat;
import org.aksw.commons.lock.db.api.ReadWriteLockWithOwnership;
import org.aksw.commons.lock.db.api.ResourceLock;
import org.aksw.commons.util.lock.LockUtils;


/**
 * A {@link ReadWriteLock} implementation that coordinates acquisition of
 * the management lock and subsequent modification of the read/write lock states.
 *
 * @author raven
 *
 */
public class LockFromLockStore
    implements ReadWriteLockWithOwnership
{
    protected ResourceLock<String> resourceLock;

    // Time to live for management locks
    protected Duration mgmtLockTtl = Duration.ofSeconds(10);

    protected String ownerKey;
    protected LockFromLink mgmtLock;
    protected LockFromLink readLock;
    protected LockFromLink writeLock;

    protected Lock readLockView = new LockView(false);
    protected Lock writeLockView = new LockView(true);


    public LockFromLockStore(
            ResourceLock<String> resourceLock,
            String ownerKey,
            LockFromLink mgmtLock,
            LockFromLink readLock,
            LockFromLink writeLock) {
        super();
        this.resourceLock = resourceLock;
        this.ownerKey = ownerKey;
        this.mgmtLock = mgmtLock;
        this.readLock = readLock;
        this.writeLock = writeLock;
    }

    @Override
    public Lock readLock() {
        return readLockView;
    }

    @Override
    public Lock writeLock() {
        return writeLockView;
    }

    @Override
    public boolean ownsReadLock() {
        boolean result = readLock.isOwnedHere();
        return result;
    }

    @Override
    public boolean ownsWriteLock() {
        boolean result = writeLock.isOwnedHere();
        return result;
    }

    @Override
    public Lock getMgmtLock() {
        return mgmtLock;
    }


    public boolean checkIfLockingIsNeeded(boolean write) {
        // Check whether we already own the lock
        boolean ownsR = ownsReadLock();
        boolean ownsW = ownsWriteLock();

        boolean needLock = true;

        if (ownsR) {
            if (write) {
                unlock();
            } else {
                needLock = false;
            }
        } else if (ownsW) {
            needLock = false;
        }

        return needLock;

    }


    protected boolean lock(boolean write) {
        return LockUtils.runWithMgmtLock(mgmtLock, LockFromLink::forceUnlock, mgmtLockTtl, () -> lockCore(write));
    }

    protected boolean lockCore(boolean write) {

        boolean result;

        // Path writeLockPath = resShadowAbsPath.resolve("write.lock");
        String writeLockOwnerKey = writeLock.readOwnerKey();
        if (writeLockOwnerKey != null && !ownerKey.equals(writeLockOwnerKey)) {
            throw new RuntimeException("Lock for " + ownerKey + " failed because write lock at " + writeLock.getPath() + " owned by " + writeLockOwnerKey);
        }

        if (!write) { // read lock requested
            result = readLock.singleLockAttempt();
        } else {
            boolean existsReadLock;
            try (Stream<String> stream = resourceLock.streamReadLockOwnerKeys()) {
                existsReadLock = stream.findAny().isPresent();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (existsReadLock) {
                throw new RuntimeException("Read lock already exists next to " + writeLock.getPath());
            } else {
                result = writeLock.singleLockAttempt();
            }
        }
        return result;
    }

    protected void unlock(boolean write) {
        LockUtils.runWithMgmtLock(mgmtLock, LockFromLink::forceUnlock, mgmtLockTtl, () -> { unlockCore(write); return null; });
    }

    protected void unlockCore(boolean write) {
        if (write) {
            writeLock.unlock();
        } else {
            readLock.unlock();
        }
    }


    public void unlock() {
        readLock().unlock();
        writeLock().unlock();
    }


    public class LockView
        extends LockBaseRepeat
    {
        protected boolean write;

        public LockView(boolean write) {
            super();
            this.write = write;
        }

//		@Override
//		protected boolean runLockAttempt() {
//			boolean result = checkIfLockingIsNeeded(false);
//			return result;
//		}

        @Override
        public boolean singleLockAttempt() throws InterruptedException {
            boolean r = LockFromLockStore.this.lock(write);
            return r;
        }

        @Override
        public void unlock() {
            LockFromLockStore.this.unlock(write);
        }
    }
}
