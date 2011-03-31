package org.aksw.commons.jena;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import java.util.List;

/**
 * Created by Claus Stadler
 * Date: Oct 21, 2010
 * Time: 9:57:03 PM
 */
public abstract class DecoratorResultSet
    implements ResultSet
{
    protected ResultSet decoratee;

    public DecoratorResultSet(ResultSet decoratee)
    {
        this.decoratee = decoratee;
    }

    @Override
    public boolean hasNext() {
        return decoratee.hasNext();
    }

    @Override
    public QuerySolution next() {
        return decoratee.next();
    }

    @Override
    public void remove() {
        decoratee.remove();
    }

    @Override
    public QuerySolution nextSolution() {
        return decoratee.nextSolution();
    }

    @Override
    public Binding nextBinding() {
        return decoratee.nextBinding();
    }

    @Override
    public int getRowNumber() {
        return decoratee.getRowNumber();
    }

    @Override
    public List<String> getResultVars() {
        return decoratee.getResultVars();
    }

    @Override
    public Model getResourceModel() {
        return decoratee.getResourceModel();
    }
}

