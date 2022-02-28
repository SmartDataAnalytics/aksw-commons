package org.aksw.commons.rx.cache.range;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.aksw.commons.util.array.ArrayOps;

import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;

public abstract class SliceBase<A>
    implements SliceWithAutoSync<A>
{

    protected ArrayOps<A> arrayOps;

    // A read/write lock for synchronizing reads/writes to the slice
    protected ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    // A condition that is signalled whenever content or metadata changes
    protected Condition hasDataCondition = readWriteLock.writeLock().newCondition();

    protected abstract SliceMetaDataBasic getMetaData();

    @Override
    public RangeSet<Long> getLoadedRanges() {
        return getMetaData().getLoadedRanges();
    }

    @Override
    public RangeMap<Long, List<Throwable>> getFailedRanges() {
        return getMetaData().getFailedRanges();
    }

    @Override
    public long getMinimumKnownSize() {
        return getMetaData().getMinimumKnownSize();
    }

    @Override
    public void setMinimumKnownSize(long size) {
        getMetaData().setMinimumKnownSize(size);
    }

    @Override
    public long getMaximumKnownSize() {
        return getMetaData().getMaximumKnownSize();
    }

    @Override
    public void setMaximumKnownSize(long size) {
        getMetaData().setMaximumKnownSize(size);
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    @Override
    public Condition getHasDataCondition() {
        return hasDataCondition;
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return arrayOps;
    }
}
