package org.aksw.commons.sparql.api.core;

import com.hp.hpl.jena.query.ResultSet;
import org.aksw.commons.collections.IClosable;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 9/7/12
 *         Time: 1:07 AM
 */
public class ResultSetClosable
    extends ResultSetClose
{
    private IClosable closable;

    public ResultSetClosable(ResultSet decoratee, IClosable closable) {
        super(decoratee);
        this.closable = closable;
        checkClose();
    }


    @Override
    public void close() {
        closable.close();
    }
}
