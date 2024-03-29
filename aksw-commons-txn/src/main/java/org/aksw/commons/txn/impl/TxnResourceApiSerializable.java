package org.aksw.commons.txn.impl;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Instant;

import org.aksw.commons.io.util.PathUtils;
import org.aksw.commons.lock.db.api.ReadWriteLockWithOwnership;
import org.aksw.commons.lock.db.api.ResourceLock;
import org.aksw.commons.util.array.Array;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Api to a resource w.r.t. a transaction.
 *
 *
 * @author raven
 *
 */
public class TxnResourceApiSerializable
    extends TxnResourceApiReadUncommitted<TxnSerializable>
{
    private static final Logger logger = LoggerFactory.getLogger(TxnResourceApiSerializable.class);

    protected Path journalEntryFile;


    protected ResourceLock<String> resourceLock;
    protected ReadWriteLockWithOwnership txnResourceLock;

    protected Cache<org.aksw.commons.path.core.Path<String>, Boolean> accessCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build();



    //		public ResourceApi(String resourceName) {
        //this.resourceName = resourceName;
    public TxnResourceApiSerializable(TxnSerializable txn, org.aksw.commons.path.core.Path<String> resKey) {// Path resFilePath) {
        super(txn, resKey);

        String resKeyStr = resKey.toString(); // PathUtils.join(resKey);

        resourceLock = txn.txnMgr.lockStore.getLockForResource(resKeyStr);
        txnResourceLock = resourceLock.get(txn.txnId);


        String[] resLockKey = txn.txnMgr.lockRepo.getPathSegments(resKeyStr);
        String resLockKeyStr = PathUtils.join(resLockKey);


//		resShadowPath = txnMgr.resShadow.getRelPath(resourceName);
//		resFilename = StringUtils.urlEncode(resourceName);

        journalEntryFile = txn.txnFolder.resolve("." + resLockKeyStr);
    }

    @Override
    public ReadWriteLockWithOwnership getTxnResourceLock() {
        return txnResourceLock;
    }

    @Override
    public boolean isVisible() {
        boolean result;

        if (txnResourceLock.isLockedHere()) {
            result = true;
        } else {
            Instant txnTime = txn.getCreationDate();
            Instant resTime;
            try {
                resTime = fileSync.getLastModifiedTime();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // If the resource's modified time is null then it did not exist yet
            result = resTime != null && resTime.isBefore(txnTime);
        }

        return result;
    }

    @Override
    public void declareAccess() {
        try {
            accessCache.get(resKey, () -> {
                declareAccessCore();
                return true;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void declareAccessCore() {
        // Path actualLinkTarget = txnFolder.relativize(resShadowAbsPath);
        Path actualLinkTarget = txn.txnFolder.relativize(resFileAbsPath);
        try {
            if (Files.exists(journalEntryFile, LinkOption.NOFOLLOW_LINKS)) {
                boolean verifyAccess = false;

                if (verifyAccess) {
                    logger.debug("Verifying access " + journalEntryFile);
                    // Verify
                    Path link = txn.txnMgr.symlinkStrategy.readSymbolicLink(journalEntryFile);
                    if (!link.equals(actualLinkTarget)) {
                        throw new RuntimeException(String.format("Validation failed: Attempted to declare access to %s but a different %s already existed ", actualLinkTarget, link));
                    }
                }
            } else {
                logger.debug("Declaring access from " + journalEntryFile + " to " + actualLinkTarget);
                FileUtilsExtra.ensureParentFolderExists(journalEntryFile, () -> {
                    try {
                        txn.txnMgr.symlinkStrategy.createSymbolicLink(journalEntryFile, actualLinkTarget);
                    } catch (FileAlreadyExistsException e) {
                        // Ignore
                        // TODO Verify whether the existing symlink matches the one we wanted to write?
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void undeclareAccess() {
        accessCache.invalidate(resKey);
        try {
            // TODO Use delete instead and log an exception?
            Files.deleteIfExists(journalEntryFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}