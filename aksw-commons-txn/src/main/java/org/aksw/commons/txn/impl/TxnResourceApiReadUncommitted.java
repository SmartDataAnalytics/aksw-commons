package org.aksw.commons.txn.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.function.Consumer;

import org.aksw.commons.io.util.PathUtils;
import org.aksw.commons.lock.db.api.ReadWriteLockWithOwnership;
import org.aksw.commons.lock.db.impl.LockOwnerDummy;
import org.aksw.commons.txn.api.TxnResourceApi;

/**
 * Api to a resource w.r.t. a transaction.
 *
 *
 * @author raven
 *
 */
public class TxnResourceApiReadUncommitted<T extends TxnReadUncommitted>
    implements TxnResourceApi
{
    protected T txn;

    protected org.aksw.commons.path.core.Path<String> resKey;
    // protected String resFilename;

    protected Path resFilePath;
    protected Path resFileAbsPath;

    // Declare an access attempt to the resource in the txn's journal

    protected FileSyncImpl fileSync;

    //		public ResourceApi(String resourceName) {
        //this.resourceName = resourceName;
    public TxnResourceApiReadUncommitted(T txn, org.aksw.commons.path.core.Path<String> resKey) {// Path resFilePath) {
        this.txn = txn;
        this.resKey = resKey;
        // this.resFilePath = resFilePath;
        //resFilePath = txnMgr.resRepo.getRelPath(resourceName);

        // String readLockFileName = "txn-" + txnId + "read.lock";

        resFileAbsPath = PathUtils.resolve(txn.txnMgr.getRootPath(), resKey.getSegments()).normalize();

        // FIXME HACK - the data.trig should probably come from elsewhere
        // fileSync = FileSyncImpl.create(resFileAbsPath.resolve("data.trig"), true);

        fileSync = FileSyncImpl.create(resFileAbsPath, true);
    }

    @Override
    public ReadWriteLockWithOwnership getTxnResourceLock() {
        return new LockOwnerDummy();
    }

    @Override
    public Instant getLastModifiedDate() throws IOException {
        return fileSync.getLastModifiedTime();
    }

    @Override
    public org.aksw.commons.path.core.Path<String> getResourceKey() {
        return resKey;
    }

//	public Path getResFilePath() {
//		return resFilePath;
//	};


    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void declareAccess() {
    }


    @Override
    public void undeclareAccess() {
    }

    @Override
    public FileSyncImpl getFileSync() {
        return fileSync;
    }

    public void putContent(Consumer<OutputStream> handler) throws IOException {
        fileSync.putContent(handler);
    }

    @Override
    public void preCommit() throws Exception {
        fileSync.preCommit();
    }

    @Override
    public void finalizeCommit() throws Exception {
        fileSync.finalizeCommit();
    }

    @Override
    public void rollback() throws Exception {
        fileSync.rollback();
    }
}