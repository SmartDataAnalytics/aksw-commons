package org.aksw.commons.txn.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

import org.aksw.commons.io.util.FileUtils;
import org.aksw.commons.txn.api.Txn;
import org.aksw.commons.txn.api.TxnMgr;
import org.aksw.commons.txn.api.TxnResourceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Skeleton for implementing commit / rollback actions. Provides callbacks
 * that allow for syncing in-memory copies.
 *
 * @author raven
 *
 */
public class TxnHandler {

    private static final Logger logger = LoggerFactory.getLogger(TxnHandler.class);

    protected TxnMgr txnMgr;


    public TxnHandler(TxnMgr txnMgr) {
        super();
        this.txnMgr = txnMgr;
    }

    protected void beforePreCommit(org.aksw.commons.path.core.Path<String> resKey) throws Exception {

    }

    protected void afterPreCommit(org.aksw.commons.path.core.Path<String> resKey) throws Exception {

    }

    protected void beforeUnlock(org.aksw.commons.path.core.Path<String> resKey, boolean isCommit) throws Exception {

    }

    protected void end() {

    }

    public void cleanupStaleTxns() throws IOException {
        logger.info("Checking existing txns...");
        try (Stream<Txn> stream = txnMgr.streamTxns()) {
            stream.forEach(txn -> {
                try {
                    // if (txn.isStale()) {
                    if (txn.claim()) {
                        rollbackOrEnd(txn);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to process txn", e);
                }
            });
        }
    }


    public void commit(Txn txn) {
        try {
            // TODO Non-write transactions can probably skip the sync block - or?
            try (Stream<org.aksw.commons.path.core.Path<String>> stream = txn.streamAccessedResourcePaths()) {
                Iterator<org.aksw.commons.path.core.Path<String>> it = stream.iterator();
                while (it.hasNext()) {
                    org.aksw.commons.path.core.Path<String> relPath = it.next();
                    logger.debug("Syncing: " + relPath);
                    // Path relPath = txnMgr.getResRepo().getRelPath(res);

                    TxnResourceApi api = txn.getResourceApi(relPath);
                    if (api.getTxnResourceLock().ownsWriteLock()) {
                        // If we own a write lock and the state is dirty then sync
                        // If there are any in memory changes then write them out

                        beforePreCommit(relPath);
                        // Precommit: Copy any new data files to their final location (but keep backups)
                        ContentSync fs = api.getFileSync();
                        fs.preCommit();

                        afterPreCommit(relPath);
                        // Update the in memory cache
                    }
                }
            }
                // Once all modified graphs are written out
                // add the statement that the commit action can now be run
                txn.addCommit();

                applyJournal(txn);
        } catch (Exception e) {
            try {
                if (txn.isCommit()) {
                    throw new RuntimeException("Failed to finalize commit after pre-commit", e);
                } else {
                    txn.addRollback();
                }
            } catch (Exception e2) {
                e2.addSuppressed(e);
                throw new RuntimeException(e2);
            }

            try {
                applyJournal(txn);
            } catch (Exception e2) {
                e2.addSuppressed(e);
                throw new RuntimeException(e2);
            }

            throw new RuntimeException(e);
        } finally {
            end();
        }
    }


    /**
     *
     * @param txn
     * @param resourceAction (resourceKey, isCommit) An action to run on a resource after changes were rolled back or committed -
     *        but before the resource is unlocked. Typically used to synchronize an in-memory cache.
     */
    public void applyJournal(Txn txn) { //LoadingCache<Array<String>, SyncedDataset> syncCache) {
        TxnMgr txnMgr = txn.getTxnMgr();
        // ResourceRepository<String> resRepo = txnMgr.getResRepo();
        Path resRepoRootPath = txnMgr.getRootPath();

        boolean isCommit;
        try {
            isCommit = txn.isCommit() && !txn.isRollback();
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }

        try {

            // Run the finalization actions
            // As these actions remove undo information
            // there is no turning back anymore
            if (isCommit) {
                txn.addFinalize();
            }

            // TODO Stream the relPaths rather than the string resource names?
            try (Stream<org.aksw.commons.path.core.Path<String>> stream = txn.streamAccessedResourcePaths()) {
                Iterator<org.aksw.commons.path.core.Path<String>> it = stream.iterator();
                while (it.hasNext()) {
                    org.aksw.commons.path.core.Path<String> res = it.next();
                    logger.debug("Finalizing and unlocking: " + res);
                    TxnResourceApi api = txn.getResourceApi(res);

                    org.aksw.commons.path.core.Path<String> resourceKey = api.getResourceKey();

                    Path targetFile = api.getFileSync().getTargetFile();
                    if (isCommit) {
                        api.finalizeCommit();
                    } else {
                        api.rollback();
                    }

                    // Clean up empty paths
                    FileUtils.deleteEmptyFolders(targetFile.getParent(), resRepoRootPath, true);

                    beforeUnlock(resourceKey, isCommit);
//                    SyncedDataset synced = syncCache.getIfPresent(Array.wrap(resourceKey));
//                    if (synced != null) {
//                        if (synced.isDirty()) {
//                            if (isCommit) {
//                                synced.getDiff().materialize();
//                            } else {
//                                synced.getDiff().clearChanges();
//                            }
//                            synced.updateState();
//                        }
//                    }

                    api.unlock();
                    api.undeclareAccess();
                }
            }

            txn.cleanUpTxn();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void abort(Txn txn) {
        try {
            txn.addRollback();
            applyJournal(txn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            end();
        }
    }

    public void rollbackOrEnd(Txn txn) throws IOException {
        logger.info("Detected stale txn; applying rollback: " + txn.getId());
        if (!txn.isCommit()) {
            txn.addRollback();
        }
        applyJournal(txn);
    }

}
