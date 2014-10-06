package org.aksw.commons.graph;


import com.hp.hpl.jena.graph.Triple;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public /**
 * A cache that only operates on single triples (rather than complete
 * graph patterns which may be composed of multiple triples)
 * 
 * The 
 * 
 * @author raven
 * 
 * TODO Maybe rename to ITripleCacheGraphIndex 
 */
interface ITripleCacheIndex
{
	IGraph getGraph();


	/**
	 * Indexes the triples. Completeness of the partitions is assumed.
	 * 
	 * @param triples
	 */
	void index(Collection<Triple> triples);

	void registerMisses(Set<List<Object>> keys);
	
	
	/**
	 * Addes triples to the cache.
	 * The level indicates whether new index partitions created from these
	 * triples are incomplete (so there might be more triples in the underlying
	 * graph, which have not been touched so far), or complete. 
	 * 
	 * @param triples
	 * @param level
	 */
	void addSeen(Collection<Triple> triples);
	/**
	 * Methods which are used to notify the index about new triples
	 * 
	 * @param triples
	 */
	void removeSeen(Collection<Triple> triples);
	
	/**
	 * Returns the compatibility level for a given pattern
	 * 
	 * 
	 * @param triple
	 * @return
	 */
	@Deprecated
	IndexCompatibilityLevel getCompatibilityLevel(Triple pattern);

	
	IndexCompatibilityLevel getCompatibilityLevel(int[] columnIds);
	
	/**
	 * Returns the "columns" that are indexed - columns correspond to
	 * 0 = subject, 1 = predicate, 2 = object
	 * 
	 * @return
	 */
	public int[] getKeyColumns();

	
	/**
	 * Returns the "columns" for which values are provided by this index
	 * 
	 * 
	 * @return
	 */
	public int[] getValueColumns();
	
	/**
	 * Retrieves and caches data for the given triple patterns.
	 * All triple patterns must be compatible with the index.
	 * 
	 * 
	 * @param triple
	 * @return
	 */
	//Multimap<List<Object>, List<Object>> bulkFind(Collection<List<Object>> keys, int[] indexColumns);
	
	Collection<Triple> lookup(List<Object> key);
	
	//Set<Triple> bulkFind(Set<Triple> patterns);
	
	void clear();
	
	

	CacheState getState();
}
