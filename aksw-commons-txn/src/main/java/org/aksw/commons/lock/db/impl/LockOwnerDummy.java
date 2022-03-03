package org.aksw.commons.lock.db.impl;

import java.util.concurrent.locks.Lock;

import org.aksw.commons.lock.db.api.ReadWriteLockWithOwnership;

public class LockOwnerDummy
	implements ReadWriteLockWithOwnership
{
	@Override
	public Lock readLock() {
		return DummyLock.INSTANCE;
	}

	@Override
	public Lock writeLock() {
		return DummyLock.INSTANCE;
	}

	@Override
	public Lock getMgmtLock() {
		return DummyLock.INSTANCE;
	}

	@Override
	public boolean ownsReadLock() {
		return true;
	}

	@Override
	public boolean ownsWriteLock() {
		return true;
	}
}
