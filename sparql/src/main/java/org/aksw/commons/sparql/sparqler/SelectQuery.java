package org.aksw.commons.sparql.sparqler;

import com.hp.hpl.jena.query.ResultSet;


/**
 * Playing around with modelling.
 * Trying to figue out whether capsulating query executions into
 * their own classes makes sense.
 * The reason is, that I want to set timeouts on a per-query level.
 * 
 * However, actually, timeouts are something that should be intrinsic to
 * sparqlers. If different timeouts are desired, than different sparqlers
 * have to be used.
 * For this purpose, a sparqler factory could be used.
 * 
 * @author raven
 *
 */
public class SelectQuery {
	private Sparqler sparqler;
	private String queryString;
	
	public SelectQuery(Sparqler sparqler, String queryString)
	{
		this.sparqler = sparqler;
		this.queryString = queryString;
	}
	
	//void setTimeOut();
	public ResultSet execute()
	{
		return sparqler.executeSelect(queryString);
	}
}
