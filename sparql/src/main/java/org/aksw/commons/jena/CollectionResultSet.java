package org.aksw.commons.jena;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Claus Stadler
 * Date: Oct 20, 2010
 * Time: 7:08:06 PM
 */
public class CollectionResultSet
    implements ResultSet
{
    private List<String> resultVars;
    private int rowId = 0;
    private Iterator<QuerySolution> iterator;

    public CollectionResultSet(List<String> resultVars, Iterable<QuerySolution> querySolutions)
    {
        this.resultVars = resultVars;
        this.iterator = querySolutions.iterator();
    }


    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public QuerySolution next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    @Override
    public QuerySolution nextSolution() {
        return next();
    }

    @Override
    public Binding nextBinding() {
        return null;
        //next().
    }

    @Override
    public int getRowNumber() {
        return rowId;
    }

    @Override
    public List<String> getResultVars() {
        return resultVars;
    }

    @Override
    public Model getResourceModel() {
        return null;
    }
}
