package org.aksw.commons.sparql.api.pagination.core;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.core.QueryExecutionFactoryBackQuery;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 11:41 PM
 */
public class QueryExecutionFactoryPaginated
    extends QueryExecutionFactoryBackQuery
{
    private QueryExecutionFactory decoratee;
    private long pageSize = 500;

    public QueryExecutionFactoryPaginated(QueryExecutionFactory decoratee, long pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        return new QueryExecutionPaginated(decoratee, query, pageSize);
    }

    /*
    @Override
    public QueryExecution createQueryExecution(String queryString) {
        return decoratee.createQueryExecution(queryString);
    }*/

    @Override
    public String getId() {
        return decoratee.getId();
    }

    @Override
    public String getState() {
        return decoratee.getState();
    }

}
