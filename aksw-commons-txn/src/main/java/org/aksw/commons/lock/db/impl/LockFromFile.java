package org.aksw.commons.lock.db.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.aksw.commons.lambda.throwing.ThrowingConsumer;
import org.aksw.commons.lock.LockBaseRepeat;
import org.aksw.commons.lock.LockManagerPath;

// Remove in favor of LockFromLink?
public class LockFromFile
    extends LockBaseRepeat
{
    protected Path path;
    protected ThrowingConsumer<? super Path> fileCreator;

    public LockFromFile(Path path) {
        super();
        this.path = path;
        this.fileCreator = p -> Files.createFile(p); //, StandardOpenOption.CREATE, StandardOpenOption.DSYNC);
    }

    public LockFromFile(Path path, ThrowingConsumer<Path> fileCreator) {
        super();
        this.path = path;
        this.fileCreator = fileCreator;
    }

    public Path getPath() {
        return path;
    }

    /**
     * First, attempt to create the process lock file.
     * If the manager already owns it then this step succeeds immediately without further waiting.
     *
     * Afterwards, attempt to get the thread lock
     *
     */
//	@Override
//	public boolean tryLock(long time, TimeUnit unit) {
//		boolean result = LockManagerPath.tryCreateLockFile(path, time, unit);
//		return result;
//	}

    @Override
    public boolean singleLockAttempt() throws InterruptedException {
        boolean result = LockManagerPath.tryCreateFile(path);
        return result;
    }

    @Override
    public void unlock() {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
