package org.aksw.commons.sparql.sparqler;


/**
 * Interface for executing queries both on string and query level.
 * 
 * @author raven
 *
 */
public interface Sparqler
	extends SparqlerString, SparqlerQuery
{
	// createStatement().executeQuery(blah);
}
