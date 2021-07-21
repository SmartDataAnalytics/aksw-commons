package org.aksw.commons.rx.cache.range;

import java.nio.file.Paths;
import java.time.Duration;

import org.aksw.jena_sparql_api.lookup.ListPaginator;

// @Ignore
public class TestSmartRangeCache
    extends RangeCacheTestSuite
{
    @Override
    protected <T> ListPaginator<T> wrapWithCache(ListPaginator<T> backend) {

        return SmartRangeCacheImpl.wrap(
                backend, SmartRangeCacheImpl.createKeyObjectStore(Paths.get("/tmp/test")), 1024, 10, Duration.ofSeconds(1), 10000, 1000);
    }
}
