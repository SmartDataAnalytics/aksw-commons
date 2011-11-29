package org.aksw.commons.sparql.api.cache.extra;

import org.apache.commons.compress.bzip2.CBZip2InputStream;
import org.apache.commons.compress.bzip2.CBZip2OutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/28/11
 *         Time: 11:26 PM
 */
public class CacheCoreExBZip2
    implements CacheCoreEx
{
    private CacheCoreEx decoratee;

    public CacheCoreExBZip2(CacheCoreEx decoratee) {
        this.decoratee = decoratee;
    }

    public static CacheCoreExBZip2 wrap(CacheCoreEx decoratee) {
        return new CacheCoreExBZip2(decoratee);
    }


    @Override
    public CacheEntry lookup(String service, String queryString) {
        CacheEntry raw = decoratee.lookup(service, queryString);

        return new CacheEntry(raw.getTimestamp(), raw.getLifespan(), new InputStreamProviderBZip2(raw.getInputStreamProvider()));
    }

    @Override
    public void write(String service, String queryString, InputStream in) {
        try {
            _write(service, queryString, in);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    // FIXME We are storing data in memory here - will break with huge amounts of data
    public void _write(String service, String queryString, InputStream in)
            throws Exception
    {
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        CBZip2OutputStream out = new CBZip2OutputStream(tmp);
        final byte[] buffer = new byte[1024];
        int n = 0;
        while (-1 != (n = in.read(buffer))) {
            out.write(buffer, 0, n);
        }
        out.close();


        decoratee.write(service, queryString, new ByteArrayInputStream(tmp.toByteArray()));
    }
}
