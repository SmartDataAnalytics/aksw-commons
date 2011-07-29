package org.aksw.commons.sparql.api;

import static junit.framework.Assert.*;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.util.ResultSetUtils;
import org.aksw.commons.sparql.api.cache.core.QueryExecutionFactoryCache;
import org.aksw.commons.sparql.api.cache.extra.Cache;
import org.aksw.commons.sparql.api.cache.extra.CacheCore;
import org.aksw.commons.sparql.api.cache.extra.CacheCoreH2;
import org.aksw.commons.sparql.api.cache.extra.CacheImpl;
import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.commons.sparql.api.http.QueryExecutionFactoryHttp;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/27/11
 *         Time: 12:27 AM
 */
public class SparqlTest {

    public QueryExecutionFactory createService() {
        String service = "http://dbpedia.org/sparql";
        List<String> defaultGraphNames = Arrays.asList("http://dbpedia.org");
        QueryExecutionFactory f = new QueryExecutionFactoryHttp(service, defaultGraphNames);

        return f;
    }

    @Test
    public void testHttp() {
        String service = "http://dbpedia.org/sparql";
        List<String> defaultGraphNames = Arrays.asList("http://dbpedia.org");
        QueryExecutionFactory f = new QueryExecutionFactoryHttp(service, defaultGraphNames);

        assertEquals("http://dbpedia.org", f.getState());
        assertEquals("http://dbpedia.org/sparql", f.getId());

        QueryExecution qe = f.createQueryExecution("Select * {?s ?p ?o .} limit 3");
        ResultSet rs = qe.execSelect();
        System.out.println(ResultSetFormatter.asText(rs));
    }

    @Test
    public void testHttpDelay() {
        QueryExecutionFactory f = createService();

        long delay = 5000;
        f = new QueryExecutionFactoryDelay(f, delay);

        long start = System.currentTimeMillis();

        ResultSetFormatter.consume(f.createQueryExecution("Select * {?s ?p ?o .} limit 3").execSelect());
        ResultSetFormatter.consume(f.createQueryExecution("Select * {?s ?p ?o .} limit 3").execSelect());

        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed > 0.9f * delay);

    }

    @Test
    public void testHttpDelayCache()
    {
        /*
        QueryExecutionFactory f = createService();

        long delay = 5000;
        f = new QueryExecutionFactoryDelay(f, delay);

        CacheCore cacheCore = CacheCoreH2.create();
        Cache cache = new CacheImpl(cacheCore);

        f = new QueryExecutionFactoryCache(f);
        */
        // TBD
    }

    @Test
    public void testHttpDelayCachePagination() {
        // TBD
    }
}
