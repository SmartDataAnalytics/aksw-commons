package org.aksw.commons.sparql.api.core;


import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 9:39 PM
 */
public abstract class QueryExecutionFactoryBackQuery<T extends QueryExecution>
    implements QueryExecutionFactory<T>
{
    @Override
    public T createQueryExecution(String queryString) {
        return createQueryExecution(QueryFactory.create(queryString));
    }
}
