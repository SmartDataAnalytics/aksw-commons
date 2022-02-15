package org.aksw.commons.store.object.key.api;

import org.aksw.commons.store.object.key.impl.ObjectInfo;
import org.aksw.commons.txn.impl.PathDiffState;
import org.aksw.commons.txn.impl.PathState;
import org.aksw.commons.util.ref.RefFuture;

public interface ObjectResource
	extends AutoCloseable
{
	/**
	 * Binds the lifetime of the content to this object: Claimed content will not be evicted from the content cache and
	 * will thus not spill to disk.
	 * Content will remain in memory which prevents possible throttling (quick successions of access, loading, eviction causing the next access to load again)
	 * 
	 * Calling {@link #close()} unclaims content.
	 */
//	void claimContent();
//	boolean isContentClaimed();
//	void unclaimContent();
	
	RefFuture<ObjectInfo> claimContent();
	
	ObjectInfo loadNewInstance();
	
	// Whether the cached resource has been loaded
	boolean isCachedInstancePresent();
	
	Object getContent();
	void setContent(Object obj);

	PathState getLoadTimeStatus();

	// Return information about the original source and any pending modification
	PathDiffState getRecencyStatus();
	void setRecencyStatus(PathDiffState status);

	PathDiffState fetchRecencyStatus();

	
	boolean hasChanged();
	Object reloadRaw();
	Object getRaw();

	void markAsDirty();
	boolean isDirty();
	
	
	/** Write changes to disk. Does not commit a transaction; changes may still be undone by a rollback. */
	void save();
	
	default <T> T reload() {
		@SuppressWarnings("unchecked")
		T result = (T)reloadRaw();
		return result;
	}
	
	default <T> T get() {
		@SuppressWarnings("unchecked")
		T result = (T)getRaw();
		return result;
	}
	
	
}
