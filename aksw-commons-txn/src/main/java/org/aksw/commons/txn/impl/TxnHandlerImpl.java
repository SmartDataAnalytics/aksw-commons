package org.aksw.commons.txn.impl;

import java.io.IOException;

import org.aksw.commons.txn.api.Txn;
import org.aksw.commons.txn.api.TxnMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Skeleton for implementing commit / rollback actions. Provides callbacks
 * that allow for syncing in-memory copies.
 *
 * @author raven
 *
 */
public class TxnHandlerImpl
    implements TxnHandler
{
    static final Logger logger = LoggerFactory.getLogger(TxnHandlerImpl.class);

    protected TxnMgr txnMgr;

    public TxnHandlerImpl(TxnMgr txnMgr) {
        super();
        this.txnMgr = txnMgr;
    }

    public void cleanupStaleTxns() throws IOException {
        TxnUtils.cleanupStaleTxns(txnMgr, this);
    }

    public void commit(Txn txn) {
        TxnUtils.commit(txn, this);
    }

    public void abort(Txn txn) {
        TxnUtils.abort(txn, this);
    }

    public void rollbackOrEnd(Txn txn) throws IOException {
        TxnUtils.rollbackOrEnd(txn, this);
    }
}
