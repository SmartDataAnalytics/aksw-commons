package org.aksw.commons.sparql.api.qe;


import com.clarkparsia.pellet.sparqldl.engine.QueryEngine;
import com.hp.hpl.jena.query.QueryExecution;
import org.aksw.commons.sparql.api.core.QueryExecutionFactoryBackString;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 9:48 PM
 */
/*
public abstract class QueryExecutionFactoryQe
    extends QueryExecutionFactoryBackString
{
    private QueryEngine qe;

    public QueryExecutionFactoryQe(QueryEngine qe) {
        this.qe = qe;
    }

    protected abstract QueryExecution createJenaQueryExecution(String queryString);


    /*
    @Override
    public String getId() {
    }* /

    @Override
    public String getState() {
        return qe.ge
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        return createJenaQueryExecution(queryString);
    }
}
*/