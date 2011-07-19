package org.aksw.commons.sparql.sparqler;

import org.aksw.commons.jena.ExtendedQueryEngineHTTP;

import com.hp.hpl.jena.query.QueryExecution;

public class SparqlerHttp
	extends SparqlerQueryExecution
{
	private String service;
	private long timeOut = 0l;
	
	public SparqlerHttp(String service)
	{
		this.service = service;
	}

	/**
	 * 
	 * @param service
	 * @param timeOut in milliseconds
	 */
	public SparqlerHttp(String service, long timeOut)
	{
		this.service = service;
		this.timeOut = timeOut;
	}
	
	@Override
	protected QueryExecution createQueryExecution(String queryString) {
		ExtendedQueryEngineHTTP result = new ExtendedQueryEngineHTTP(service, queryString);
		result.setTimeOut(timeOut);
		return result;
	}

	@Override
	public Object getId() {
		return service;
	}
}