package org.aksw.commons.io.cache;

import java.time.Duration;

public class AdvancedRangeCacheConfigImpl
    implements AdvancedRangeCacheConfig
{
    protected int pageSize;
    protected int maxRequestSize;
    protected Duration terminationDelay;
    protected int internalWorkerSize;

    public AdvancedRangeCacheConfigImpl() {
    }

    public AdvancedRangeCacheConfigImpl(int pageSize, int maxRequestSize, Duration terminationDelay,
            int internalWorkerSize) {
        super();
        this.pageSize = pageSize;
        this.maxRequestSize = maxRequestSize;
        this.terminationDelay = terminationDelay;
        this.internalWorkerSize = internalWorkerSize;
    }

    public static AdvancedRangeCacheConfigImpl createDefault() {
        return new AdvancedRangeCacheConfigImpl(10000, 10000, Duration.ofSeconds(2), 128);
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
    public int getMaxRequestSize() {
        return maxRequestSize;
    }

    public AdvancedRangeCacheConfigImpl setMaxRequestSize(int maxRequestSize) {
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + internalWorkerSize;
        result = prime * result + maxRequestSize;
        result = prime * result + pageSize;
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
                + ", terminationDelay=" + terminationDelay + ", internalWorkerSize=" + internalWorkerSize + "]";
    }
}
