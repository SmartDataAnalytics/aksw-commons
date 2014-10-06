package org.aksw.commons.sparql.api.delay.core;


import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.core.QueryExecutionFactoryDecorator;
import org.aksw.commons.sparql.api.core.QueryExecutionStreaming;
import org.aksw.commons.sparql.api.delay.extra.Delayer;
import org.aksw.commons.sparql.api.delay.extra.DelayerDefault;

import com.hp.hpl.jena.query.Query;

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
public class QueryExecutionFactoryDelay
        extends QueryExecutionFactoryDecorator {
    private Delayer delayer;

    public QueryExecutionFactoryDelay(QueryExecutionFactory decoratee) {
        this(decoratee, 1000);
    }

    public QueryExecutionFactoryDelay(QueryExecutionFactory decoratee, long delay) {
        this(decoratee, new DelayerDefault(delay));
    }

    public QueryExecutionFactoryDelay(QueryExecutionFactory decoratee, Delayer delayer) {
        super(decoratee);
        this.delayer = delayer;
    }

    @Override
    public QueryExecutionStreaming createQueryExecution(Query query) {
        return new QueryExecutionDelay(super.createQueryExecution(query), delayer);
    }

    @Override
    public QueryExecutionStreaming createQueryExecution(String queryString) {
        return new QueryExecutionDelay(super.createQueryExecution(queryString), delayer);
    }
}
