package org.aksw.commons.sparql.sparqler;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;


/**
 * Converts all query strings to Query objects, and invokes
 * the appropriate method.
 * 
 * @author raven
 *
 */
public abstract class SparqlerFrontString
	implements Sparqler
{
	public SparqlerFrontString()
	{
	}
	
	@Override
	public ResultSet executeSelect(String queryString) {
		return executeSelect(QueryFactory.create(queryString));
	}

	@Override
	public boolean executeAsk(String queryString) {
		return executeAsk(QueryFactory.create(queryString));
	}


	@Override
	public Model executeConstruct(Model result, String queryString) {
		return executeConstruct(result, QueryFactory.create(queryString));		
	}

	@Override
	public Model executeDescribe(Model result, String queryString) {
		return executeDescribe(result, QueryFactory.create(queryString));				
	}

	@Override
	public void executeUpdate(String queryString) {
		UpdateRequest request = new UpdateRequest();
		UpdateFactory.parse(request, queryString);

		executeUpdate(request);
	}
}

