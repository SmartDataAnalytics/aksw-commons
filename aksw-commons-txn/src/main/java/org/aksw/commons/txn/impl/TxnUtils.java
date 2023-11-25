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
 * Static methods for running commits and rollbacks on the {@link Txn} API.
 */
public class TxnUtils {
    private static final Logger logger = LoggerFactory.getLogger(TxnUtils.class);

    public static void cleanupStaleTxns(TxnMgr txnMgr, TxnHandler handler) throws IOException {
        if (logger.isInfoEnabled()) {
            logger.info("Checking existing txns...");
        }
        try (Stream<Txn> stream = txnMgr.streamTxns()) {
            stream.forEach(txn -> {
                try {
                    // if (txn.isStale()) {
                    if (txn.claim()) {
                        TxnUtils.rollbackOrEnd(txn, handler);
                    }
                } catch (Exception e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Failed to process txn", e);
                    }
                }
            });
        }
    }

    public static void precommit(Txn txn, TxnHandler handler) throws Exception, IOException {
        // TODO Non-write transactions can probably skip the sync block - or?
        try (Stream<org.aksw.commons.path.core.Path<String>> stream = txn.streamAccessedResourcePaths()) {
            Iterator<org.aksw.commons.path.core.Path<String>> it = stream.iterator();
            while (it.hasNext()) {
                org.aksw.commons.path.core.Path<String> relPath = it.next();
                // Path relPath = txnMgr.getResRepo().getRelPath(res);

                TxnResourceApi api = txn.getResourceApi(relPath);
                if (api.getTxnResourceLock().ownsWriteLock()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Syncing: " + relPath);
                    }
                    // If we own a write lock and the state is dirty then sync
                    // If there are any in memory changes then write them out

                    handler.beforePreCommit(relPath);
                    // Precommit: Copy any new data files to their final location (but keep backups)
                    ContentSync fs = api.getFileSync();
                    fs.preCommit();

                    handler.afterPreCommit(relPath);
                    // Update the in memory cache
                }
            }
        }
    }

    public static void commit(Txn txn, TxnHandler handler) {
        try {
            precommit(txn, handler);
            // Once all modified graphs are written out
            // add the statement that the commit action can now be run
            txn.addCommit();

            TxnUtils.applyJournal(txn, handler);
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
                TxnUtils.applyJournal(txn, handler);
            } catch (Exception e2) {
                e2.addSuppressed(e);
                throw new RuntimeException(e2);
            }

            throw new RuntimeException(e);
        } finally {
            handler.end();
        }
    }

    /**
         *
         * @param txn
         * @param resourceAction (resourceKey, isCommit) An action to run on a resource after changes were rolled back or committed -
         *        but before the resource is unlocked. Typically used to synchronize an in-memory cache.
         */
        @Deprecated // Old version does not separate finalization and unlocking
        // The advantage of the old version is that resources are unlocked earlier which could
        // reduce latency
        public static void applyJournalOld(Txn txn, TxnHandler handler) { //LoadingCache<Array<String>, SyncedDataset> syncCache) {
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
                        if (logger.isDebugEnabled()) {
                            logger.debug("Finalizing and unlocking: " + res);
                        }
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

                        handler.beforeUnlock(resourceKey, isCommit);
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

    /** */
    public static void applyJournal(Txn txn, TxnHandler handler) {
        TxnUtils.finalizeCommit(txn);
        TxnUtils.unlock(txn, handler);
    }

    /** Finalize all pending changes. All files of the transaction will be in their final state.
     */
    public static void finalizeCommit(Txn txn) { //LoadingCache<Array<String>, SyncedDataset> syncCache) {
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
                    if (logger.isDebugEnabled()) {
                        logger.debug("Finalizing: " + res);
                    }
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
                }
            }

            txn.cleanUpTxn();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void unlock(Txn txn, TxnHandler handler) { //LoadingCache<Array<String>, SyncedDataset> syncCache) {
        boolean isCommit;
        try {
            isCommit = txn.isCommit() && !txn.isRollback();
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }

        try {
            // TODO Stream the relPaths rather than the string resource names?
            try (Stream<org.aksw.commons.path.core.Path<String>> stream = txn.streamAccessedResourcePaths()) {
                Iterator<org.aksw.commons.path.core.Path<String>> it = stream.iterator();
                while (it.hasNext()) {
                    org.aksw.commons.path.core.Path<String> res = it.next();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Unlocking: " + res);
                    }
                    TxnResourceApi api = txn.getResourceApi(res);

                    org.aksw.commons.path.core.Path<String> resourceKey = api.getResourceKey();

                    handler.beforeUnlock(resourceKey, isCommit);
                    api.unlock();
                    api.undeclareAccess();
                }
            }

            txn.cleanUpTxn();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void abort(Txn txn, TxnHandler handler) {
        try {
            txn.addRollback();
            applyJournal(txn, handler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            handler.end();
        }
    }

    public static void rollbackOrEnd(Txn txn, TxnHandler handler) throws IOException {
        if (logger.isInfoEnabled()) {
            logger.info("Detected stale txn; applying rollback: " + txn.getId());
        }
        if (!txn.isCommit()) {
            txn.addRollback();
        }
        applyJournal(txn, handler);
    }
}
