package org.aksw.commons.sparql.api.core;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 9/20/12
 *         Time: 1:41 PM
 */
public class QueryExecutionStreamingFactory
        extends  QueryExecutionFactoryBackQuery<QueryExecution>
{
    protected QueryExecutionFactory decoratee;

    public QueryExecutionStreamingFactory(QueryExecutionFactory decoratee) {
        this.decoratee = decoratee;
    }


    @Override
    public String getId() {
        return decoratee.getId();
    }

    @Override
    public String getState() {
        return decoratee.getState();
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        QueryExecution tmp = decoratee.createQueryExecution(query);
        QueryExecution result = new QueryExecutionAdapterStreaming(tmp);
        return result;
    }
}
