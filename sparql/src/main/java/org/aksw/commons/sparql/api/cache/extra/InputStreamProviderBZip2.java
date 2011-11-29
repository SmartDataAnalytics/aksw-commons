package org.aksw.commons.sparql.api.cache.extra;

import org.apache.commons.compress.bzip2.CBZip2InputStream;

import java.io.InputStream;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/28/11
 *         Time: 11:53 PM
 */
public class InputStreamProviderBZip2
    implements InputStreamProvider
{
    private InputStreamProvider decoratee;

    public InputStreamProviderBZip2(InputStreamProvider decoratee)
    {
        this.decoratee = decoratee;
    }

    @Override
    public InputStream open() {
        return new CBZip2InputStream(decoratee.open());
    }

    @Override
    public void close() {
        decoratee.close();
    }
}
