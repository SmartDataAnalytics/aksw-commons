package org.aksw.commons.graph;

import com.hp.hpl.jena.graph.Triple;

import java.util.Collection;

public interface IGraphListener
{
	void onAdd(IGraph g, Collection<Triple> triples);
	void onRemove(IGraph g, Collection<Triple> triples);
}
