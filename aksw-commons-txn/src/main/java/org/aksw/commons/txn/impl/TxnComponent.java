package org.aksw.commons.txn.impl;

public interface TxnComponent {
    void preCommit() throws Exception;
    void finalizeCommit() throws Exception;
    void rollback() throws Exception;

    // void preRollback(); Not useful: If rollback fails there is hardly anything worth restoring
}
