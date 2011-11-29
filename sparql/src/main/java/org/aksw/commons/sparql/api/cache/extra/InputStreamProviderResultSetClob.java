package org.aksw.commons.sparql.api.cache.extra;

import java.io.InputStream;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/28/11
 *         Time: 11:54 PM
 */
public class InputStreamProviderResultSetClob
    implements InputStreamProvider
{
    private java.sql.ResultSet rs;
    private Clob clob;

    public InputStreamProviderResultSetClob(java.sql.ResultSet rs, Clob clob) {
        this.rs = rs;
        this.clob = clob;
    }


    @Override
    public InputStream open() {
        try {
            return clob.getAsciiStream();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        SqlUtils.close(rs);
    }
}
