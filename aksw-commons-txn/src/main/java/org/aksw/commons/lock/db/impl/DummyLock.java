package org.aksw.commons.lock.db.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.aksw.commons.lock.LockBase;

public class DummyLock
	extends LockBase
{
	public static final Lock INSTANCE = new DummyLock();
	
	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		return true;
	}

	@Override
	public void unlock() {
	}
}
