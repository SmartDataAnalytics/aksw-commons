package org.aksw.commons.sparql.api.core;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;

import java.util.Iterator;
import java.util.Set;


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 9/20/12
 *         Time: 1:29 PM
 */
public class QueryExecutionAdapterStreaming
    extends QueryExecutionDecorator
    implements QueryExecutionStreaming
{

    public QueryExecutionAdapterStreaming(QueryExecution queryExecution) {
        super(queryExecution);
    }

    public static Iterator<Triple> createTripleIterator(Model model) {
        Set<Triple> set = model.getGraph().find(null, null, null).toSet();
        return set.iterator();
    }

    @Override
    public Iterator<Triple> execConstructStreaming() {
        Model model = super.decoratee.execConstruct();
        return createTripleIterator(model);
    }

    @Override
    public Iterator<Triple> execDescribeStreaming() {
        Model model = super.decoratee.execDescribe();
        return createTripleIterator(model);
    }
}
