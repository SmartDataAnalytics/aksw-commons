package org.aksw.commons.io.slice;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;


public class SliceMetaDataImpl
    implements SliceMetaDataBasic, Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * If the value is null then the range is considered as successfully loaded.
     * If a throwable is present then there was an error processing the range
     */
    protected RangeSet<Long> loadedRanges;
    protected RangeMap<Long, List<Throwable>> failedRanges;
    protected long minimumKnownSize;
    protected long maximumKnownSize;

//    protected transient ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
//    protected transient Condition hasDataCondition = readWriteLock.writeLock().newCondition();

    public SliceMetaDataImpl() {
        this(
                TreeRangeSet.create(),
                TreeRangeMap.create(),
                0,
                Long.MAX_VALUE
        );
    }

    public SliceMetaDataImpl(RangeSet<Long> loadedRanges, RangeMap<Long, List<Throwable>> failedRanges,
            long minimumKnownSize, long maximumKnownSize) {
        super();
        this.loadedRanges = loadedRanges;
        this.failedRanges = failedRanges;
        this.minimumKnownSize = minimumKnownSize;
        this.maximumKnownSize = maximumKnownSize;
    }

//    @Override
//    public ReadWriteLock getReadWriteLock() {
//        return readWriteLock;
//    }
//
//    @Override
//    public Condition getHasDataCondition() {
//        return hasDataCondition;
//    }

    public RangeSet<Long> getLoadedRanges() {
        return loadedRanges;
    }

    public void setLoadedRanges(RangeSet<Long> loadedRanges) {
        this.loadedRanges = loadedRanges;
    }

    public RangeMap<Long, List<Throwable>> getFailedRanges() {
        return failedRanges;
    }

    public long getMinimumKnownSize() {
        return minimumKnownSize;
    }

    public long getMaximumKnownSize() {
        return maximumKnownSize;
    }

    @Override
    public void setMinimumKnownSize(long minimumKnownSize) {
        this.minimumKnownSize = minimumKnownSize;
    }

    @Override
    public void setMaximumKnownSize(long maximumKnownSize) {
        this.maximumKnownSize = maximumKnownSize;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((failedRanges == null) ? 0 : failedRanges.hashCode());
        result = prime * result + ((loadedRanges == null) ? 0 : loadedRanges.hashCode());
        result = prime * result + (int) (maximumKnownSize ^ (maximumKnownSize >>> 32));
        result = prime * result + (int) (minimumKnownSize ^ (minimumKnownSize >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SliceMetaDataImpl other = (SliceMetaDataImpl) obj;
        if (failedRanges == null) {
            if (other.failedRanges != null)
                return false;
        } else if (!failedRanges.equals(other.failedRanges))
            return false;
        if (loadedRanges == null) {
            if (other.loadedRanges != null)
                return false;
        } else if (!loadedRanges.equals(other.loadedRanges))
            return false;
        if (maximumKnownSize != other.maximumKnownSize)
            return false;
        if (minimumKnownSize != other.minimumKnownSize)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SliceMetaDataImpl [loadedRanges=" + loadedRanges + ", failedRanges=" + failedRanges
                + ", minimumKnownSize=" + minimumKnownSize + ", maximumKnownSize=" + maximumKnownSize + "]";
    }


}
