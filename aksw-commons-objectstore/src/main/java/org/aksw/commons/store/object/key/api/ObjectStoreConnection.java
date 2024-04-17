package org.aksw.commons.store.object.key.api;


import org.aksw.commons.path.core.Path;
import org.aksw.commons.txn.api.TxnApi;
import org.aksw.commons.txn.impl.TxnHandler;

public interface ObjectStoreConnection
    extends TxnApi, AutoCloseable
{
    /**
     * Declare access to the requested resource and lock it with the
     * ObjectStoreConnection's active transaction mode (read or write).
     */
    ObjectResource access(Path<String> keySegments);

    void commit(TxnHandler handler);

    // @Override
    // void close();
}
