package org.aksw.commons.graph;


import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.graph.GraphFactory;

import java.util.HashSet;
import java.util.Set;

public class TripleUtils
{
	public static Set<Triple> toTriples(Iterable<Statement> stmts)
	{
		Set<Triple> result = new HashSet<Triple>();
		for(Statement stmt : stmts) {
			result.add(stmt.asTriple());
		}
		
		return result;
	}
	
	
	public static Model toModel(Iterable<Triple> triples) {
		Graph graph = GraphFactory.createDefaultGraph();
		for(Triple triple : triples) {
			graph.add(triple);
		}
		return ModelFactory.createModelForGraph(graph);
	}

	public static Model toModel(Iterable<Triple> triples, Model model) {
		
		Model part = toModel(triples);
		model.add(part);
		
		return model;
	}

	public static Node get(Triple triple, int index)
	{
		switch(index) {
		case 0: return triple.getSubject();
		case 1: return triple.getPredicate();
		case 2: return triple.getObject();
		default: throw new IndexOutOfBoundsException();
		}
	}
}
