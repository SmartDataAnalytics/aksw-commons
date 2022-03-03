package org.aksw.commons.rx.cache.range;

import org.aksw.commons.rx.lookup.ListPaginator;

public class TestSimpleRangeCache
    extends RangeCacheTestSuite
{
    @Override
    protected <T> ListPaginator<T> wrapWithCache(String testId, ListPaginator<T> backend) {
        return ListPaginatorWithSimpleCache.wrap(backend);
    }
}
