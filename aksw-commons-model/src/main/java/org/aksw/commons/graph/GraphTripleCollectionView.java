package org.aksw.commons.graph;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;

import java.util.AbstractCollection;
import java.util.Iterator;


/**
 * A Collection&lt;Triple&gt; view over a graph.
 * 
 * Warning: The underlying iterator is not closed. TODO Maybe close at least
 * on consumption/exception of this iterator.
 * 
 * Only use with memory graphs.
 * 
 * @author raven
 *
 */
public class GraphTripleCollectionView
	extends AbstractCollection<Triple>
{
	private Graph graph;
	
	public GraphTripleCollectionView(Graph graph)
	{
		this.graph = graph;
	}

	@Override
	public Iterator<Triple> iterator()
	{
		return graph.find(null, null, null);
	}

	@Override
	public int size()
	{
		return graph.size();
	}
}
