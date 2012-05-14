package org.aksw.commons.sparql.api.compare;


import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.core.QueryExecutionFactoryBackQuery;

/**
 * A query execution factory, which generates query executions
 * that delay execution
 *
 *
 * @author Claus Stadler
 *
 *
 *         Date: 7/26/11
 *         Time: 10:27 AM
 */
public class QueryExecutionFactoryCompare
        extends QueryExecutionFactoryBackQuery { // We need to be able to parse queries in order to get the ordering
    private QueryExecutionFactory a;
    private QueryExecutionFactory b;

    public QueryExecutionFactoryCompare(QueryExecutionFactory a, QueryExecutionFactory b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        boolean isOrdered = !query.getOrderBy().isEmpty();
        return new QueryExecutionCompare(a.createQueryExecution(query), b.createQueryExecution(query), false);
    }

    /*
    @Override
    public QueryExecution createQueryExecution(String queryString) {
        return new QueryExecutionCompare(a.createQueryExecution(queryString), b.createQueryExecution(queryString), false);
    }*/

    @Override
    public String getId() {
        return "compare(" + a.getId() + ", " + b.getId() + ")";
    }

    @Override
    public String getState() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
