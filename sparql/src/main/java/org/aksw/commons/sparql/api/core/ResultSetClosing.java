package org.aksw.commons.sparql.api.core;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import org.aksw.commons.collections.IClosable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 10:54 PM
 *
 * Unlike JCDB, Jena does not provide the close method on the ResultSet, but on the object that
 * created the result set. Therefore:
 *
 * TODO Make this resultset have a reference to its creating class, so that a close method on this
 */
public class ResultSetClosing
        extends ResultSetDecorator
{
    private static final Logger logger = LoggerFactory.getLogger(ResultSetClosing.class);

    private IClosable closable;
    private boolean isClosed = false;

    public ResultSetClosing(ResultSet decoratee, IClosable closable) {
        super(decoratee);
        this.closable = closable;
        checkClose();
    }


    protected boolean checkClose() {
        if (!isClosed && !decoratee.hasNext()) {
            try {
                isClosed = true ;
                closable.close();
            }
            catch(Exception e) {
                logger.error("Error closing an object supposedly underlying a Jena ResultSet", e);
            }
        }
        return isClosed;
    }

    @Override
    public boolean hasNext() {
        return !checkClose();
    }

    @Override
    public void remove() {
        super.remove();
        checkClose();
    }


    @Override
    public QuerySolution nextSolution() {
        QuerySolution result = super.nextSolution();
        checkClose();
        return result;
    }

    @Override
    public Binding nextBinding() {
        Binding result = super.nextBinding();
        checkClose();
        return result;
    }
}