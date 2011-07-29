package org.aksw.commons.sparql.api.cache.extra;

import com.hp.hpl.jena.query.Query;

import java.sql.ResultSet;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 10:22 PM
 */
public interface Cache
{
    void write(String queryString, ResultSet resultSet);
    void write(Query query, ResultSet resultSet);

    CacheResource lookup(String queryString);
    CacheResource lookup(Query query);
}
