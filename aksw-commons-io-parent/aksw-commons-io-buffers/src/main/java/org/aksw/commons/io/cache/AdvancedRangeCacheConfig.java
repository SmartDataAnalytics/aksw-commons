package org.aksw.commons.io.cache;

import java.time.Duration;

public interface AdvancedRangeCacheConfig {
    /**
     * Cache data before the requested ranges. May greatly speed up reading backwards.
     *
     * Whenever a worker has to be created to serve a request range
     * let the worker start by this amount of items before the request range
     * @return
     */
    long getReadBeforeSize();
    int getPageSize();
    long getMaxRequestSize();
    Duration getTerminationDelay();
    int getInternalWorkerSize();
}
