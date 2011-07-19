package org.aksw.commons.sparql.sparqler;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateRequest;

public abstract class SparqlerFrontQuery
	implements Sparqler
{
	@Override
	public ResultSet executeSelect(Query query) {
		return executeSelect(query.toString());
	}

	@Override
	public boolean executeAsk(Query query) {
		return executeAsk(query.toString());
	}


	@Override
	public Model executeConstruct(Model result, Query query) {
		return executeConstruct(result, query.toString());		
	}

	@Override
	public Model executeDescribe(Model result, Query query) {
		return executeDescribe(result, query.toString());				
	}

	@Override
	public void executeUpdate(UpdateRequest requestString) {
		executeUpdate(requestString);
	}
}
