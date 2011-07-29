package org.aksw.commons.sparql.api.cache.extra;

import com.hp.hpl.jena.query.Query;

import java.sql.ResultSet;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 5:12 PM
 */
public class CacheImpl
    implements Cache
{
    private CacheCore cacheCore;

    public CacheImpl(CacheCore cacheCore) {
        this.cacheCore = cacheCore;
    }


    @Override
    public void write(String queryString, ResultSet resultSet) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void write(Query query, ResultSet resultSet) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CacheResource lookup(String queryString) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CacheResource lookup(Query query) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
