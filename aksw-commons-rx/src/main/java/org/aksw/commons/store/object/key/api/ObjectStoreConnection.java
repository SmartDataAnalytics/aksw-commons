package org.aksw.commons.store.object.key.api;

import org.aksw.commons.txn.api.TxnApi;

public interface ObjectStoreConnection
	extends TxnApi, AutoCloseable
{
	ObjectResource access(String... keySegments);


	// @Override
	// void close();
}
