package org.aksw.commons.sparql.api.pagination.core;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorCloseable;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorResultSet;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import org.aksw.commons.sparql.api.core.QueryExecutionDecorator;
import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.openjena.atlas.io.IndentedWriter;
import org.openjena.atlas.lib.Closeable;

import javax.xml.transform.Result;
import java.util.Iterator;
import java.util.List;


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
        this.pageSize = pageSize;
    }


    @Override
    public ResultSet execSelect() {
        ResultSetPaginated it = new ResultSetPaginated(this, factory, query, pageSize);
        // Note: This line forces the iterator to initialize the result set...
        it.hasNext();
        
        // ... which makes the set of resultVars available
        List<String> resultVars = it.getCurrentResultSet().getResultVars();

        QueryIterator myQueryIterator = new MyQueryIteratorWrapper(it);
        QueryIteratorCloseable itClosable = new QueryIteratorCloseable(myQueryIterator, it);

        ResultSet rs = ResultSetFactory.create(itClosable, resultVars);

        return rs;
    }
}

class MyQueryIteratorWrapper
    extends QueryIteratorBase
{
    private Iterator<Binding> it;

    public MyQueryIteratorWrapper(Iterator<Binding> it) {
        this.it = it;
    }

    @Override
    protected boolean hasNextBinding() {
        return it.hasNext();
    }

    @Override
    protected Binding moveToNextBinding() {
        return it.next();
    }

    @Override
    protected void closeIterator() {

    }

    @Override
    protected void requestCancel() {

    }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt) {
    }
}