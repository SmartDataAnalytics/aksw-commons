package org.aksw.commons.io.cache;

import java.time.Duration;

public interface AdvancedRangeCacheConfig {
    int getPageSize();
    long getMaxRequestSize();
    Duration getTerminationDelay();
    int getInternalWorkerSize();
}
