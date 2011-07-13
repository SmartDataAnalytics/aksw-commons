package org.aksw.commons.graph;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Triple;

public interface IGraph
{
	/**
	 * Adds triples to the graph
	 * 
	 * @param triple
	 */
	void add(Collection<Triple> triple);
	void remove(Collection<Triple> triple);
	
	Collection<IGraphListener> getGraphListeners();
	ICacheProvider getCacheProvider();

	Set<Triple> bulkFind(Set<List<Object>> keys, int[] indexColumns);
	//Multimap<List<Object>, List<Object>> bulkFind(Collection<List<Object>> keys, int[] indexColumns);	
	
	Set<Triple> uncachedBulkFind(Set<List<Object>> keys, int[] indexColumns);
	//Multimap<List<Object>, List<Object>> uncachedBulkFind(List<List<Object>> keys, int[] indexColumns);
			
	/**
	 * An explicitely uncached version of bulkFind.
	 * 
	 * Idealy, only a cache provider should call this method.
	 * 
	 * @param pattern
	 * @return
	 */
	//Collection<Triple> uncachedBulkFind(Collection<Triple> patterns);
	
	
	/**
	 * Removes all triples from the graph
	 * 
	 */
	void clear();
}

