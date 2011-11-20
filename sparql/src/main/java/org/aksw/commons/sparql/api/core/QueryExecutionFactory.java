package org.aksw.commons.sparql.api.core;

import com.hp.hpl.jena.query.QueryExecution;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 10:54 AM
 */
public interface QueryExecutionFactory<T extends QueryExecution>
    extends QueryExecutionFactoryString<T>, QueryExecutionFactoryQuery<T>
{
    String getId();
    String getState();
}
