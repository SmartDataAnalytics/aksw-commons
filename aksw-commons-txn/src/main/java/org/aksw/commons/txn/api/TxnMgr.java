package org.aksw.commons.txn.api;

import java.io.IOException;
import java.time.temporal.TemporalAmount;
import java.util.stream.Stream;

import org.aksw.commons.lock.db.api.LockStore;
import org.aksw.commons.txn.impl.ResourceRepository;

public interface TxnMgr
{
    String getTxnMgrId();

    LockStore<String[], String> getLockStore();

    ResourceRepository<String> getResRepo();

    /** Create a new transaction. If the id is null then an id will be allocated.
     *  If a txn with the given id already exists an exception in raised. */
    Txn newTxn(String id, boolean useJournal, boolean isWrite) throws IOException;

    default Txn newTxn (boolean useJournal, boolean isWrite) throws IOException {
        return newTxn(null, useJournal, isWrite);
    }

    /** Get an accessor to an existing transaction */
    Txn getTxn(String txnId);


    /** Stream all existing transactions */
    Stream<Txn> streamTxns() throws IOException;

    /**
     *  Method mainly (or rather only) for testing:
     *  Clears all locks and transactions regardless of any concurrent access.
     *  Resets the txn manager's state to a clean slate.
     */
    void deleteResources() throws IOException;

    TemporalAmount getHeartbeatDuration();
}
