package org.aksw.commons.lock.db.impl;

import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.function.Function;

import org.aksw.commons.io.util.FileUtils;
import org.aksw.commons.io.util.symlink.SymbolicLinkStrategy;
import org.aksw.commons.lock.LockBaseRepeat;
import org.aksw.commons.txn.impl.FileUtilsExtra;

public class LockFromLink
//	The semantic of this class is not that of a lock but of a DAO - it adds/removes lock entries to the store
    // extends LockBase
    extends LockBaseRepeat
{
    protected SymbolicLinkStrategy linkStrategy;
    protected Path path;
    protected String ownerKey;
    protected Function<String, Path> ownerKeyToTarget;
    protected Function<Path, String> targetToOwnerKey;
    protected Path cleanupAncestorPath; // A parent of 'path' up to which to delete empty directories

    public LockFromLink(
            SymbolicLinkStrategy linkStrategy,
            Path path,
            String ownerKey,
            Function<String, Path> ownerKeyToTarget,
            Function<Path, String> targetToOwnerKey,
            Path cleanupAncestorPath) {
        super();
        this.linkStrategy = linkStrategy;
        this.path = path;
        this.ownerKey = ownerKey;
        this.ownerKeyToTarget = ownerKeyToTarget;
        this.targetToOwnerKey = targetToOwnerKey;
        this.cleanupAncestorPath = cleanupAncestorPath;
    }


    public Path getPath() {
        return path;
    }

//	@Override
//	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
//		long ms = unit.toMillis(time);
//		long retryIntervalInMs = 100;
//		long retryCount = (ms / retryIntervalInMs) + (ms % retryIntervalInMs == 0 ? 0 : 1);
//
//		boolean result = RetryUtils.simpleRetry(retryCount, retryIntervalInMs, () -> {
//			return singleLockAttempt();
//		});
//
//		return result;
//	}

    @Override
    public boolean singleLockAttempt() {
        boolean result[] = {false};

        // Try to create the lock file
        try {
            FileUtilsExtra.ensureParentFolderExists(path, () -> {
                Path targetPath = ownerKeyToTarget.apply(ownerKey);
                try {
                    linkStrategy.createSymbolicLink(path, targetPath);
                    result[0] = true;
                } catch (FileAlreadyExistsException e) {
                    String currentOwnerKey = readOwnerKey();

                    if (ownerKey.equals(currentOwnerKey)) {
                        result[0] = true;
                        // Nothing todo; we already own the lock
                    } else {
                        throw new RuntimeException("Cannot acquire lock at " + targetPath + " for owner " + ownerKey + " because it is owned by '" + currentOwnerKey + "'");
                    }
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result[0];
    }

    // @Override
    public void unlock() {
        String currentOwnerKey = readOwnerKey();
        if (currentOwnerKey != null) {
            if (!ownerKey.equals(currentOwnerKey)) {
                throw new RuntimeException("Cannot unlock a lock with owner " + ownerKey + " because it is owned by " + currentOwnerKey);
            }

            forceUnlock();
        }
    }

    // Returns true if an unlock occurred
    public boolean forceUnlock() {
        try {
            boolean result = FileUtils.deleteFileIfExistsAndThenDeleteEmptyFolders(path, cleanupAncestorPath, true);
            // boolean result = Files.deleteIfExists(path);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String readOwnerKey() {
        String result;
        try {
            Path target = linkStrategy.readSymbolicLink(path);
            result = targetToOwnerKey.apply(target);
        } catch (NoSuchFileException e) {
            result = null;
        } catch (AccessDeniedException e) {
            // FIXME vfs2nio raises this exception when NoSuchFileException would be more approprate
            result = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public boolean isOwnedHere() {
        String currentOwnerKey = readOwnerKey();
        boolean result = ownerKey.equals(currentOwnerKey);
        return result;
    }


    @Override
    public String toString() {
        return "LockFromLink [path=" + path + "]";
    }


//	public boolean isOwnedElsewhere() {
//		String currentOwnerKey = readOwnerKey();
//		boolean result = currentOwnerKey != null && !ownerKey.equals(currentOwnerKey);
//		return result;
//	}

}
