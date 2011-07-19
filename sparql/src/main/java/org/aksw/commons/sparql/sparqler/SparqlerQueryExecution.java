package org.aksw.commons.sparql.sparqler;

import org.apache.commons.lang.NotImplementedException;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;


public abstract class SparqlerQueryExecution
	extends SparqlerFrontQuery
{
	protected abstract QueryExecution createQueryExecution(String queryString);
	
	protected QueryExecution runningQueryExecution;
	
	public synchronized QueryExecution initQueryExecution(String queryString) 
	{
		this.runningQueryExecution = createQueryExecution(queryString);
		return this.runningQueryExecution; 
	}

	
	@Override
	public synchronized boolean executeAsk(String queryString) {
		boolean result = initQueryExecution(queryString).execAsk();
		this.runningQueryExecution = null;
		
		return result;
	}

	@Override
	public synchronized Model executeDescribe(Model result, String queryString) {
		result = initQueryExecution(queryString).execDescribe();
		this.runningQueryExecution = null;
		
		return result;
	}

	@Override
	public synchronized Model executeConstruct(Model result, String queryString) {
		result = initQueryExecution(queryString).execConstruct(result);
		this.runningQueryExecution = null;

		return result;
	}

	@Override
	public synchronized ResultSet executeSelect(String queryString) {
		ResultSet result = initQueryExecution(queryString).execSelect();
		this.runningQueryExecution = null;

		return result;
	}

	@Override
	public synchronized void executeUpdate(String requestString) {
		throw new NotImplementedException();
	}
	
	@Override
	public void abort() {
		synchronized (runningQueryExecution) {
			if(runningQueryExecution != null) {
				runningQueryExecution.abort();
				runningQueryExecution = null;
			}
		}
	}
}
