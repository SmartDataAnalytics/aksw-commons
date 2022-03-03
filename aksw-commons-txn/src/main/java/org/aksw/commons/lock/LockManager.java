package org.aksw.commons.lock;

import java.util.concurrent.locks.Lock;

public interface LockManager<T> {
	Lock getLock(T resource, boolean write);
}
