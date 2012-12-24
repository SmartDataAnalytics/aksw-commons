package org.aksw.commons.sparql.api.cache.core;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import org.aksw.commons.sparql.api.cache.extra.Cache;
import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.core.QueryExecutionFactoryDecorator;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 4:08 PM
 */
public class QueryExecutionFactoryCache
        extends QueryExecutionFactoryDecorator
{
    private Cache cache;

    public QueryExecutionFactoryCache(QueryExecutionFactory decoratee, Cache cache) {
        super(decoratee);
        this.cache = cache;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        return new QueryExecutionCache(super.createQueryExecution(query), query.toString(), cache);
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        return new QueryExecutionCache(super.createQueryExecution(queryString), queryString, cache);
    }
}
