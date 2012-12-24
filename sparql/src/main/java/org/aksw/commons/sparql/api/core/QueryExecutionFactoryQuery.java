package org.aksw.commons.sparql.api.core;


import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 9:30 PM
 */
public interface QueryExecutionFactoryQuery<T extends QueryExecution> {
    T createQueryExecution(Query query);
}
