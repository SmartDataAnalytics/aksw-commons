package org.aksw.commons.graph;

import java.util.Collection;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;

public interface IGraphListener
{
	void onAdd(IGraph g, Collection<Triple> triples);
	void onRemove(IGraph g, Collection<Triple> triples);
}
