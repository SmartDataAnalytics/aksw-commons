package org.aksw.commons.lock;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

public class LockManagerCompound<T>
	implements LockManager<T>
{
	protected List<? extends LockManager<T>> delegates;
	
	public LockManagerCompound(List<? extends LockManager<T>> delegates) {
		super();
		this.delegates = delegates;
	}

	@Override
	public Lock getLock(T resource, boolean write) {
		List<Lock> locks = delegates.stream()
				.map(lockMgr -> lockMgr.getLock(resource, write))
				.collect(Collectors.toList());
		
		return new CompoundLock(locks);
	}
}
