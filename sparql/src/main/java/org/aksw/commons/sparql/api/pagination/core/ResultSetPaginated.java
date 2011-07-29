package org.aksw.commons.sparql.api.pagination.core;


import com.hp.hpl.jena.query.*;
import org.aksw.commons.collections.PrefetchIterator;
import org.aksw.commons.collections.SinglePrefetchIterator;
import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.pagination.extra.PaginationState;

import java.util.Iterator;

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
	extends SinglePrefetchIterator<ResultSet>
{
	private QueryExecutionFactory service;
	private QueryExecutionPaginated execution;

    private PaginationState state;


	public ResultSetPaginated(QueryExecutionPaginated execution,QueryExecutionFactory service, String queryString, long pageSize) {
		this(execution, service, QueryFactory.create(queryString), pageSize);
	}

	public ResultSetPaginated(QueryExecutionPaginated execution, QueryExecutionFactory service, Query query, long pageSize) {
		this.execution = execution;
        this.service = service;
		this.state = new PaginationState(query, pageSize);
	}

	@Override
	protected ResultSet prefetch() throws Exception {
		Query query = state.next();
		if(query == null) {
			return finish();
		}

        QueryExecution qe = service.createQueryExecution(query);

        if(execution != null) {
            execution._setDecoratee(qe);
        }

		ResultSet result = qe.execSelect();
        if(!result.hasNext()) {
            return finish();
        }

        return result;
	}	
}

