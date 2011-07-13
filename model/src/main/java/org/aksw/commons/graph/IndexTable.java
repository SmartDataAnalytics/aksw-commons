package org.aksw.commons.graph;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a cache entry for a certain index key.
 * The complete flag indicates whether the store may contain additional data
 * that is not in the index.
 * 
 * Note: A "real" implementation would take orderings into account
 * 
 * @author raven
 */
public class IndexTable {
	private boolean isComplete = false;
	private Set<List<Object>> rows = new HashSet<List<Object>>();
	
	public IndexTable() {
	}

	public IndexTable(boolean isComplete) {
		this.isComplete = isComplete;
	}
	
	public boolean isComplete()
	{
		return isComplete;
	}
	
	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}
	
	public Set<List<Object>> getRows() {
		return rows;
	}
}