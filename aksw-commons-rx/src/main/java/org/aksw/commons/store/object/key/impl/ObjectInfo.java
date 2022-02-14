package org.aksw.commons.store.object.key.impl;

import java.time.Instant;

import org.aksw.commons.txn.impl.FileSync;
import org.aksw.commons.txn.impl.PathDiffState;

public class ObjectInfo {
	protected Object obj;
	protected int initialHashCode; // Cached value of obj's hashCode.
	protected FileSync fileSync;
	
	protected PathDiffState status;
	
	protected Instant lastChange;
	// protected Object newObj;
	
	// protected ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	
	public ObjectInfo(Object obj, int initialHashCode, PathDiffState status) {
		super();
		this.obj = obj;
		this.initialHashCode = initialHashCode;
		this.status = status;
		// this.lastChange = status.getCurrentState().getTimestamp();
	}	
	
	public <T> T getObject() {
		return (T)obj;
	}
	
//	@SuppressWarnings("unchecked")
//	public <T> T getNewObject() {
//		return (T)newObj;
//	}
	
//	public ReadWriteLock getReadWriteLock() {
//		return readWriteLock;
//	}
}