package org.aksw.commons.store.object.key.api;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.store.object.key.impl.ObjectInfo;
import org.aksw.commons.txn.impl.PathDiffState;
import org.aksw.commons.util.ref.RefFuture;

public interface ObjectStore
    extends AutoCloseable
{
    /**
     * Asynchronously attempt to claim to the given resource.
     * @param key
     * @return
     */
    RefFuture<ObjectInfo> claim(Path<String> key);

    // Get the recency status of a resource outside of any transaction
    PathDiffState fetchRecencyStatus(Path<String> key);

    ObjectStoreConnection getConnection();
}
