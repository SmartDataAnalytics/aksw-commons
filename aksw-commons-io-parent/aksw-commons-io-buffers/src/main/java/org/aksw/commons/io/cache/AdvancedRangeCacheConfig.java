package org.aksw.commons.io.cache;

import java.time.Duration;

public interface AdvancedRangeCacheConfig {
    int getPageSize();
    int getMaxRequestSize();
    Duration getTerminationDelay();
    int getInternalWorkerSize();
}
