package org.aksw.commons.sparql.sparqler;

public interface QueryExecution {
	/**
	 * Abort a currently running query execution.
	 * If a result set was obtained, it will be closed.
	 * If no result set was obtained, then it will not be possible
	 * to obtain it.
	 */
	public void abort();
}
