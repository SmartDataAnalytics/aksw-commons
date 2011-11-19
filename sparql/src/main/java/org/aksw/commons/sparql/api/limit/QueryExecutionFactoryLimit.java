package org.aksw.commons.sparql.api.limit;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.core.QueryExecutionFactoryBackQuery;
import org.aksw.commons.sparql.api.core.QueryExecutionFactoryDecorator;

/**
 * A query execution that sets a limit on all queries
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/19/11
 *         Time: 11:33 PM
 */
public class QueryExecutionFactoryLimit
    extends QueryExecutionFactoryDecorator
{
    private Long limit;
    private boolean doCloneQuery = false;

    public QueryExecutionFactoryLimit(QueryExecutionFactory decoratee, boolean doCloneQuery, Long limit) {
        super(decoratee);
        this.limit = limit;
    }

    public QueryExecution createQueryExecution(Query query) {
        if(limit != null) {
            if(query.getLimit() == Query.NOLIMIT) {
                if(doCloneQuery) {
                    query = query.cloneQuery();
                }

                query.setLimit(limit);
            } else {
                long adjustedLimit = Math.min(limit, query.getLimit());

                if(adjustedLimit != query.getLimit()) {
                    if(doCloneQuery) {
                        query = query.cloneQuery();
                    }

                    query.setLimit(adjustedLimit);
                }
            }
        }

        return super.createQueryExecution(query);
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        return createQueryExecution(QueryFactory.create(queryString));
    }

}
