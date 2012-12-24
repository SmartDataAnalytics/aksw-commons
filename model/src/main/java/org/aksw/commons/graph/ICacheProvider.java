package org.aksw.commons.graph;

import com.hp.hpl.jena.graph.Triple;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ICacheProvider
{
	IGraph getGraph();
	
	//Set<Triple> bulkFind(Collection<Triple> patterns);
	//Multimap<List<Object>, List<Object>> bulkFind(Collection<List<Object>> keys, int[] indexColumns);
	Set<Triple> bulkFind(Set<List<Object>> keys, int[] indexColumns);
	
	Collection<ITripleCacheIndex> getIndexes();

	/**
	 * Adds seen triples to the cache.
	 * Partitions are updated accodingly.
	 * The completeness state of partitions is left unaffected.
	 * 
	 * Do not call these methods directly - only the graph should do this.
	 * 
	 * @param triples
	 */
	void addSeen(Collection<Triple> triples);
	void removeSeen(Collection<Triple> triples);
	
	/**
	 * Clears all caches
	 */
	void clear();
}
