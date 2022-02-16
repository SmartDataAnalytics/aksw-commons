package org.aksw.commons.store.object.key.api;

import org.aksw.commons.store.object.key.impl.ObjectInfo;
import org.aksw.commons.txn.impl.PathDiffState;
import org.aksw.commons.util.ref.RefFuture;

public interface ObjectStore
	extends AutoCloseable
{
	RefFuture<ObjectInfo> claim(String... key);
	
	// Get the recency status of a resource outside of any transaction
	PathDiffState fetchRecencyStatus(String ...key);
	
	ObjectStoreConnection getConnection();	
}
