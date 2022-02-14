package org.aksw.commons.store.object.key.api;

import org.aksw.commons.store.object.key.impl.ObjectInfo;
import org.aksw.commons.util.ref.RefFuture;

public interface ObjectStore
	extends AutoCloseable
{
	RefFuture<ObjectInfo> claim(String... key);
	
	ObjectStoreConnection getConnection();	
}
