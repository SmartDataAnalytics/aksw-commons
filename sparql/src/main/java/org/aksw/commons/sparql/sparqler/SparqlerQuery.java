package org.aksw.commons.sparql.sparqler;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.Update;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * Core interface for creating result sets from Jena query objects.
 * 
 * @author raven
 *
 */
public interface SparqlerQuery {
	boolean executeAsk(Query query);
	Model executeDescribe(Model result, Query query);
	Model executeConstruct(Model result, Query query);
	ResultSet executeSelect(Query query);
	
	void executeUpdate(UpdateRequest request);

	Object getId();

	void abort();
}
