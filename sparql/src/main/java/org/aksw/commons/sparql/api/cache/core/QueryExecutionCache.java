package org.aksw.commons.sparql.api.cache.core;

import java.io.IOException;

import org.aksw.commons.sparql.api.cache.extra.Cache;
import org.aksw.commons.sparql.api.cache.extra.CacheResource;
import org.aksw.commons.sparql.api.core.QueryExecutionDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 4:11 PM
 */
public class QueryExecutionCache
    extends QueryExecutionDecorator
{
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionCache.class);


    private Cache cache;
    private String queryString;

    public QueryExecutionCache(QueryExecution decoratee, String queryString, Cache cache) {
        super(decoratee);

        this.queryString = queryString;
        this.cache = cache;
    }


    public ResultSet doCacheResultSet()
    {
        CacheResource resource = cache.lookup(queryString);

        ResultSet rs;
        if(resource == null || resource.isOutdated()) {

            try {
                rs = getDecoratee().execSelect();
            } catch(Exception e) {
                logger.warn("Error communicating with backend", e);

                if(resource != null) {
                    //logger.trace("Cache hit for " + queryString);
                    return resource.asResultSet();
                } else {
                    throw new RuntimeException(e);
                }
            }

            logger.trace("Cache write: " + queryString);
            cache.write(queryString, rs);
            resource = cache.lookup(queryString);
            if(resource == null) {
                throw new RuntimeException("Cache error: Lookup of just written data failed");
            }

        } else {
            logger.trace("Cache hit: " + queryString);
        }

        return resource.asResultSet();
    }

    public Model doCacheModel(Model result) {
        try {
            return _doCacheModel(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Model _doCacheModel(Model result) throws IOException {
        CacheResource resource = cache.lookup(queryString);

        Model model = ModelFactory.createDefaultModel();
        if(resource == null || resource.isOutdated()) {

            try {
                model = getDecoratee().execConstruct();
            } catch(Exception e) {
                logger.warn("Error communicating with backend", e);

                if(resource != null) {
                    model = resource.asModel(model);
                    result.add(model);
                    return result;
                } else {
                    throw new RuntimeException(e);
                }
            }

            logger.trace("Cache write: " + queryString);
            cache.write(queryString, model);
            resource = cache.lookup(queryString);
            if(resource == null) {
                throw new RuntimeException("Cache error: Lookup of just written data failed");
            }
        } else {
            logger.trace("Cache hit: " + queryString);
        }

        return resource.asModel(result);
    }
    
    public boolean doCacheBoolean()
    {
        CacheResource resource = cache.lookup(queryString);

        boolean ret;
        if(resource == null || resource.isOutdated()) {

            try {
                ret = getDecoratee().execAsk();
            } catch(Exception e) {
                logger.warn("Error communicating with backend", e);

                if(resource != null) {
                    //logger.trace("Cache hit for " + queryString);
                    return resource.asBoolean();
                } else {
                    throw new RuntimeException(e);
                }
            }

            logger.trace("Cache write: " + queryString);
            cache.write(queryString, ret);
            resource = cache.lookup(queryString);
            if(resource == null) {
                throw new RuntimeException("Cache error: Lookup of just written data failed");
            }

        } else {
            logger.trace("Cache hit: " + queryString);
        }

        return resource.asBoolean();
    }


    @Override
     public ResultSet execSelect() {
        return doCacheResultSet();
     }

     @Override
     public Model execConstruct() {
         return doCacheModel(ModelFactory.createDefaultModel());
     }

     @Override
     public Model execConstruct(Model model) {
        return doCacheModel(model);
     }

     @Override
     public Model execDescribe() {
         return super.execDescribe();
     }

     @Override
     public Model execDescribe(Model model) {
         return super.execDescribe(model);
     }

     @Override
     public boolean execAsk() {
         return doCacheBoolean();
     }
}
