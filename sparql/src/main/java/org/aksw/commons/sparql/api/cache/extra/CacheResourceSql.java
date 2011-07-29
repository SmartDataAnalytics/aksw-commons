package org.aksw.commons.sparql.api.cache.extra;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import org.aksw.commons.collections.IClosable;
import org.aksw.commons.sparql.api.core.ResultSetClosing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Clob;
import java.sql.SQLException;


class ClosableCacheSql
    implements IClosable
{
    private CacheResource resource;
    private InputStream in;

    public ClosableCacheSql(CacheResource resource, InputStream in) {
        this.resource = resource;
        this.in = in;
    }


    @Override
    public void close() {
        //SqlUtils.close(rs);
        resource.close();
        if(in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 3:46 PM
 */
public class CacheResourceSql
    extends CacheResourceBase
{
    private static Logger logger = LoggerFactory.getLogger(CacheResourceSql.class);
    private java.sql.ResultSet rs;
    private Clob clob;

    public CacheResourceSql(long timestamp, long lifespan, java.sql.ResultSet rs, Clob clob) {
        super(timestamp, lifespan);

        this.rs = rs;
        this.clob = clob;
    }


    /*
    @Override
    public InputStream open()  {
        try {
            return clob.getAsciiStream();
        } catch (SQLException e) {
            return null;
        }
    }*/

    /**
     * This class streams the result set.
     * Take care to close it. It auto-closes on consumption.
     *
     * @return
     */
    @Override
    public ResultSet asResultSet() {
        try {
            return _asResultSet();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public ResultSet _asResultSet()
            throws SQLException
    {
        InputStream in = clob.getAsciiStream();
        return new ResultSetClosing(ResultSetFactory.fromXML(in), new ClosableCacheSql(this, in));
    }

    @Override
    public Model asModel(Model result) {
        try {
            return _asModel(result);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Model _asModel(Model result) throws SQLException {
        InputStream in = clob.getAsciiStream();

	    result.read(in, null, "N-TRIPLE");
        try {
            in.close();
        } catch (Exception e) {
            logger.warn("Error", e);
        }
        this.close();

        return result;
    }


    @Override
    public void close() {
        SqlUtils.close(rs);
    }
}
