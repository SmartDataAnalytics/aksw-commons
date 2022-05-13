package org.aksw.commons.io.cache;

import java.time.Duration;

public class AdvancedRangeCacheConfigImpl
    implements AdvancedRangeCacheConfig
{
    protected int pageSize;
    protected long maxRequestSize;
    protected Duration terminationDelay;
    protected int internalWorkerSize;

    // Not yet wired up; read items before the requested offset
    protected long readBeforeSize;

    public AdvancedRangeCacheConfigImpl() {
    }

    public AdvancedRangeCacheConfigImpl(int pageSize, long maxRequestSize, Duration terminationDelay,
            int internalWorkerSize) {
        super();
        this.pageSize = pageSize;
        this.maxRequestSize = maxRequestSize;
        this.terminationDelay = terminationDelay;
        this.internalWorkerSize = internalWorkerSize;
    }

    /**
     * PageSize            : 100000 objects
     * MaxRequestsize      : unlimited
     * Sync delay          : 5 seconds
     * Internal buffer size: 1024 objects
     *
     * Values subject to change.
     *
     * @return
     */
    public static AdvancedRangeCacheConfigImpl newDefaultsForObjects() {
        return newDefaultsForObjects(Long.MAX_VALUE);
    }

    public static AdvancedRangeCacheConfigImpl newDefaultsForObjects(long requestSize) {
        return new AdvancedRangeCacheConfigImpl(100000, requestSize, Duration.ofSeconds(5), 1024);
    }

    /**
     * PageSize            : 16MB
     * MaxRequestsize      : unlimited
     * Sync delay          : 5 seconds
     * Internal buffer size: 8KB
     *
     * Values subject to change.
     *
     * @return
     */
    public static AdvancedRangeCacheConfigImpl newDefaultForBytes() {
        return newDefaultForBytes(Long.MAX_VALUE);
    }

    public static AdvancedRangeCacheConfigImpl newDefaultForBytes(long requestSize) {
        return new AdvancedRangeCacheConfigImpl(16 * 1024 * 1024, requestSize, Duration.ofSeconds(5), 1024 * 8);
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    public AdvancedRangeCacheConfigImpl setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    @Override
    public long getMaxRequestSize() {
        return maxRequestSize;
    }

    public AdvancedRangeCacheConfigImpl setMaxRequestSize(long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
        return this;
    }

    @Override
    public Duration getTerminationDelay() {
        return terminationDelay;
    }

    public AdvancedRangeCacheConfigImpl setTerminationDelay(Duration terminationDelay) {
        this.terminationDelay = terminationDelay;
        return this;
    }

    @Override
    public int getInternalWorkerSize() {
        return internalWorkerSize;
    }

    public void setInternalWorkerSize(int internalWorkerSize) {
        this.internalWorkerSize = internalWorkerSize;
    }

    @Override
    public long getReadBeforeSize() {
        return getReadBeforeSize();
    }

    public void setReadBeforeSize(long readBeforeSize) {
        this.readBeforeSize = readBeforeSize;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + internalWorkerSize;
        result = prime * result + (int) (maxRequestSize ^ (maxRequestSize >>> 32));
        result = prime * result + pageSize;
        result = prime * result + (int) (readBeforeSize ^ (readBeforeSize >>> 32));
        result = prime * result + ((terminationDelay == null) ? 0 : terminationDelay.hashCode());
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
        AdvancedRangeCacheConfigImpl other = (AdvancedRangeCacheConfigImpl) obj;
        if (internalWorkerSize != other.internalWorkerSize)
            return false;
        if (maxRequestSize != other.maxRequestSize)
            return false;
        if (pageSize != other.pageSize)
            return false;
        if (readBeforeSize != other.readBeforeSize)
            return false;
        if (terminationDelay == null) {
            if (other.terminationDelay != null)
                return false;
        } else if (!terminationDelay.equals(other.terminationDelay))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AdvancedRangeCacheConfigImpl [pageSize=" + pageSize + ", maxRequestSize=" + maxRequestSize
                + ", terminationDelay=" + terminationDelay + ", internalWorkerSize=" + internalWorkerSize
                + ", readBeforeSize=" + readBeforeSize + "]";
    }
}
