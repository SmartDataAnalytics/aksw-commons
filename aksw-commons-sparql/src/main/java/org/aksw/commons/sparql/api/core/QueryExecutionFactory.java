package org.aksw.commons.sparql.api.core;


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 10:54 AM
 */
public interface QueryExecutionFactory
    extends QueryExecutionFactoryString, QueryExecutionFactoryQuery
{
    String getId();
    String getState();
}
