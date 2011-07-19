package org.aksw.commons.sparql.sparqler;


import java.util.Iterator;

import org.aksw.commons.collections.PrefetchIterator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;


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


public class SelectPaginated
	extends PrefetchIterator<QuerySolution>
{
	private Sparqler sparqler;
	private PaginationState state;
	
	public SelectPaginated(Sparqler sparqler, String queryString, long pageSize) {
		this(sparqler, QueryFactory.create(queryString), pageSize);
	}

	public SelectPaginated(Sparqler sparqler, Query query, long pageSize) {
		this.sparqler = sparqler;
		this.state = new PaginationState(query, pageSize);
	}

	@Override
	protected Iterator<QuerySolution> prefetch() throws Exception {
		Query query = state.next();
		if(query == null) {
			return null;
		}
		
		return sparqler.executeSelect(query);
	}	
}

/**
 *  
 * 
 * @author raven
 *
 */
class PaginationState
{	
	private long nextOffset;
	private Long nextRemaining;
	
	private Query query;
	private long pageSize;
	
	/**
	 * Note: The query object's limit and offest will be modified.
	 * Use Query.cloneQuery in order to create a copy.
	 * 
	 * @param sparqler
	 * @param query
	 * @param pageSize
	 */
	public PaginationState(Query query, long pageSize)
	{
		this.query = query;
		this.pageSize = pageSize;
		
		
		nextOffset = query.getOffset() == Query.NOLIMIT ? 0 : query.getOffset();
		nextRemaining = query.getLimit() == Query.NOLIMIT ? null : query.getLimit();	
	}
	

	/**
	 * Returns the next query or null
	 * 
	 * @return
	 * @throws Exception
	 */
	protected Query next()
			throws Exception
	{
		if(nextOffset == 0) {
			query.setOffset(Query.NOLIMIT);
		} else {
			query.setOffset(nextOffset);
		}
		
		if(nextRemaining == null) {
			query.setLimit(pageSize);
			nextOffset += pageSize;
		} else {
			long limit = Math.min(pageSize, nextRemaining);
			nextOffset += limit;
			nextRemaining -= limit;
			
			if(limit == 0) {
				return null;
			}
			
			query.setLimit(limit);
		}

		return query;
	}
}
