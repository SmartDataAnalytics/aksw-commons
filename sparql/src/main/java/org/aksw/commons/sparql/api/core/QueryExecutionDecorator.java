package org.aksw.commons.sparql.api.core;

import com.hp.hpl.jena.query.QueryExecution;

public class QueryExecutionDecorator
	extends QueryExecutionDecoratorBase<QueryExecution>
{
	public QueryExecutionDecorator(QueryExecution decoratee) {
		super(decoratee);
	}
}
