package org.aksw.commons.graph;


import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Triple;

public class MemoryGraph
	extends BaseIndexedGraph
{
	// TODO Add support for clustered indexes ;)
	// Might look something like: Set<Triple> triples = clusteredIndex.tripleView()
	private Set<Triple> triples = new HashSet<Triple>();
	
	
	@Override
	public void add(Collection<Triple> ts)
	{
		triples.addAll(ts);
		
		for(IGraphListener listener : listeners) {
			listener.onAdd(this, triples);
		}
	}

	@Override
	public void remove(Collection<Triple> ts)
	{
		//Set<Triple> actuallyRemoved = new HashSet<Triple>(ts);
		triples.removeAll(ts);
		
		//actuallyRemoved.
		
		for(IGraphListener listener : listeners) {
			listener.onRemove(this, ts);
		}		
	}

	@Override
	public Set<Triple> uncachedBulkFind(Set<List<Object>> keys,
			int[] keyColumns)
	{
		Set<Triple> result = new HashSet<Triple>();
		
		if(keyColumns.length == 0) {
			result.addAll(triples);
		} else {
	
			for(Triple triple : triples) {
				if(TripleIndexUtils.getMatchingKey(keys, triple, keyColumns) != null) {
					result.add(triple);
				}
			}
		}
		
		return result;
	}

	@Override
	public void clear()
	{
		triples.clear();
	}
}
