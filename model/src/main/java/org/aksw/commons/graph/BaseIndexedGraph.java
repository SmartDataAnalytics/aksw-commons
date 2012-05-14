package org.aksw.commons.graph;

import com.hp.hpl.jena.graph.Triple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A graph with support for indexes.
 * 
 * FIXME The management of indexes should be factored out into something
 * like IIndexManager, or IIndexProvider.
 * 
 */
public abstract class BaseIndexedGraph
	implements IGraph
{
	protected List<IGraphListener> listeners = new ArrayList<IGraphListener>();
	protected ICacheProvider cacheProvider;

	
	protected BaseIndexedGraph()
	{
		this.cacheProvider = new DefaultCacheProvider(this);
	}
		
	@Override
	public Collection<IGraphListener> getGraphListeners()
	{
		return listeners;
	}

	@Override
	public ICacheProvider getCacheProvider()
	{
		return cacheProvider;
	}
	
	@Override
	public Set<Triple> bulkFind(Set<List<Object>> keys, int[] indexColumns)
	{
		// Delegate the find to the cache provider
		// Note: It the cacheProvider has to do uncached lookups as it sees fit.		
		return (cacheProvider == null)
			? uncachedBulkFind(keys, indexColumns)
			: cacheProvider.bulkFind(keys, indexColumns);
	}
	
	@Override
	public void add(Collection<Triple> triples)
	{
		for(IGraphListener listener : listeners) {
			listener.onAdd(this, triples);
		}
	}

	@Override
	public void remove(Collection<Triple> triples)
	{
		for(IGraphListener listener : listeners) {
			listener.onRemove(this, triples);
		}		
	}
	
	@Override
	public String toString() {
		//return "Index Status:\n" + getCacheProvider().toString();
		return "Index Status:\n" + getCacheProvider().toString();
	}
}
