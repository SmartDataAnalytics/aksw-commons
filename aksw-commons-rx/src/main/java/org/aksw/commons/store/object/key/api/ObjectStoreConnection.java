package org.aksw.commons.store.object.key.api;


import org.aksw.commons.path.core.Path;
import org.aksw.commons.txn.api.TxnApi;

public interface ObjectStoreConnection
	extends TxnApi, AutoCloseable
{
	ObjectResource access(Path<String> keySegments);


	// @Override
	// void close();
}
