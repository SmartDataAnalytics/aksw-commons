package org.aksw.commons.sparql.api.pagination.core;


import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorResultSet;
import org.aksw.commons.collections.PrefetchIterator;
import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.pagination.extra.PaginationState;
import org.openjena.atlas.lib.Closeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
class ConstructPaginated
	extends PrefetchIterator<Statement>
{
	private Sparqler sparqler;
	private PaginationState state;

	public ConstructPaginated(Sparqler sparqler, String queryString, long pageSize) {
		this(sparqler, QueryFactory.create(queryString), pageSize);
	}
	
	public ConstructPaginated(Sparqler sparqler, Query query, long pageSize) {
		this.sparqler = sparqler;
		this.state = new PaginationState(query, pageSize);
	}

	@Override
	protected Iterator<Statement> prefetch() throws Exception {
		Query query = state.next();
		if(query == null) {
			return null;
		}
		
		Model model = ModelFactory.createDefaultModel();
		return sparqler.executeConstruct(model, query).listStatements();
	}	
}
*/

public class ResultSetPaginated
	extends PrefetchIterator<Binding>
    implements Closeable
{
    private static Logger logger = LoggerFactory.getLogger(ResultSetPaginated.class);

	private QueryExecutionFactory service;
	private QueryExecutionPaginated execution;

    private PaginationState state;

    private ResultSet currentResultSet = null;

	public ResultSetPaginated(QueryExecutionPaginated execution,QueryExecutionFactory service, String queryString, long pageSize) {
		this(execution, service, QueryFactory.create(queryString), pageSize);
	}

	public ResultSetPaginated(QueryExecutionPaginated execution, QueryExecutionFactory service, Query query, long pageSize) {
		this.execution = execution;
        this.service = service;
		this.state = new PaginationState(query, pageSize);
	}

    public ResultSet getCurrentResultSet() {
        return currentResultSet;
    }

	@Override
	protected QueryIteratorResultSet prefetch() throws Exception {
		Query query = state.next();
		if(query == null) {
			return null;
		}

        QueryExecution qe = service.createQueryExecution(query);

        if(execution != null) {
            execution._setDecoratee(qe);
        }

        //QueryIteratorCloseable
        logger.trace("Executing: " + query);
		currentResultSet = qe.execSelect();
        if(!currentResultSet.hasNext()) {
            return null;
        }

        return new QueryIteratorResultSet(currentResultSet);
	}

    @Override
    public void close() {
        if(execution != null) {
            execution.close();
        }
    }
}

