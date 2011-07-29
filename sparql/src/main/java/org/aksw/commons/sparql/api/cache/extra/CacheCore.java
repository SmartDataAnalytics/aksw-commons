package org.aksw.commons.sparql.api.cache.extra;

import java.io.InputStream;

/**
 * TODO Rename to CacheStorage
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 5:12 PM
 */
public interface CacheCore {
    CacheResource lookup(String queryString);
    void write(String queryString, InputStream in);

}
