package org.aksw.commons.jena;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;

public class ModelSetView
	extends AbstractSet<Statement>
{
	private Model model;

	public ModelSetView(Model model)
	{
		this.model = model;
	}

	@Override
	public Iterator<Statement> iterator()
	{
		return model.listStatements();
	}

	@Override
	public int size()
	{
		return (int)model.size();
	}
	
	@Override
	public boolean contains(Object stmt)
	{
		return (stmt instanceof Statement)
			? model.contains((Statement)stmt)
			: false;
	}
}
