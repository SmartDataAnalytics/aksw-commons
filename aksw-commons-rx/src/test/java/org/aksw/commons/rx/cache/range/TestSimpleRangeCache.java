package org.aksw.commons.rx.cache.range;

import org.aksw.jena_sparql_api.lookup.ListPaginator;

public class TestSimpleRangeCache
    extends RangeCacheTestSuite
{
    @Override
    protected <T> ListPaginator<T> wrapWithCache(String testId, ListPaginator<T> backend) {
        return SimpleRangeCache.wrap(backend);
    }
}
