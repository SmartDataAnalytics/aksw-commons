package org.aksw.commons.sparql.api.cache.extra;


import java.io.InputStream;
import java.util.Iterator;

/**
 * An interface similar to CacheCore, except that an additional service argument is supported.
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/28/11
 *         Time: 10:55 PM
 */
public interface CacheCoreEx {
    CacheEntry lookup(String service, String queryString);
    void write(String service, String queryString, InputStream in);
}
