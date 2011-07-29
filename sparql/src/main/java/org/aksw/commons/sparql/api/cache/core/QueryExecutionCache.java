package org.aksw.commons.sparql.api.cache.core;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.aksw.commons.sparql.api.cache.extra.Cache;
import org.aksw.commons.sparql.api.cache.extra.CacheResource;
import org.aksw.commons.sparql.api.core.QueryExecutionDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
                    return resource.asResultSet();
                } else {
                    throw new RuntimeException(e);
                }
            }

            //ResultSetFormatter.u
            //resource = cache.write(queryString, rs);
        }

        return resource.asResultSet();
    }

    public Model doCacheModel(Model result) {
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

            //resource = cache.write(queryString, model);
        }

        return resource.asModel(result);
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
         return super.execAsk();
     }
}
