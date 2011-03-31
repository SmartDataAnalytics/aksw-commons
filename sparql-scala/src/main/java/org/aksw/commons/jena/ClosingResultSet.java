package org.aksw.commons.jena;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Created by Claus Stadler
 * Date: Oct 21, 2010
 * Time: 8:39:43 PM
 *
 * Due to "Too many open files" problems with ResultSetFactory.fromXml(InputStream in)
 * I assume the default jena implementation doesn't close the stream
 * properly. For bugtracking I provide this implementation which
 * closes the stream as soon as the last element is fetched.
 *
 * Update: same day, 9:53:00 PM: Seems as if Jena not closing the stream
 * was the problem. Wrapping a result set and its underlying input stream
 * with this class fixed the problem.
 *
 */
public class ClosingResultSet
    extends DecoratorResultSet
{
    protected static final Logger logger = LoggerFactory.getLogger(ClosingResultSet.class);

    protected InputStream in;
    protected boolean isClosed = false;

    public ClosingResultSet(ResultSet decoratee, InputStream in)
    {
        super(decoratee);
        this.in = in;

        checkClose();
    }

    protected boolean checkClose()
    {
        if(!isClosed && !decoratee.hasNext()) {
            try {
                isClosed = true;
                in.close();
            } catch(Exception e) {
                logger.error("Error closing an InputStream supposedly underlying a Jena ResultSet", e);
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