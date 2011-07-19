package org.aksw.commons.sparql.sparqler;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Core interface for creating result sets from query strings.
 * 
 * Extend SparqlerBase for default implementations of the
 * convenience methods.
 * 
 * @author raven
 *
 */
public interface SparqlerString {
	boolean executeAsk(String queryString);
	Model executeDescribe(Model result, String queryString);
	Model executeConstruct(Model result, String queryString);
	ResultSet executeSelect(String queryString);

	void executeUpdate(String requestString);

	void abort();

	/**
	 * This method should return an object uniquely identifying
	 * the (non-transient) state of this object.
	 * This can be used for caches, by associating queries and
	 * result sets with the id.
	 * 
	 * @return
	 */
	Object getId();
}