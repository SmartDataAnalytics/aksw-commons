package org.aksw.commons.sparql.api.pagination.core;

import com.hp.hpl.jena.query.*;
import org.aksw.commons.sparql.api.core.QueryExecutionDecorator;
import org.aksw.commons.sparql.api.core.QueryExecutionFactory;

import java.util.Iterator;


/**
 * A query execution that generates paginated result sets.
 * Note: Construct queries are NOT paginated.
 * (Because I don't see how a model can be paginated)
 *
 *
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 7:59 PM
 */
public class QueryExecutionPaginated
        extends QueryExecutionDecorator
{
    private QueryExecutionFactory factory;
    private Query query;
    private long pageSize;

    private QueryExecution current;


    synchronized void _setDecoratee(QueryExecution decoratee) {
        super.setDecoratee(decoratee);
    }

    public QueryExecutionPaginated(QueryExecutionFactory factory, Query query, long pageSize) {
        super(null);
        this.query = query;
        this.factory = factory;
    }


    @Override
    public ResultSet execSelect() {
        Iterator<ResultSet> it = new ResultSetPaginated(this, factory, query, pageSize);

        return new ResultSetCombined(it);
    }
}
