package org.aksw.commons.sparql.api;

import static junit.framework.Assert.*;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.util.ResultSetUtils;
import com.hp.hpl.jena.vocabulary.RDF;
import org.aksw.commons.sparql.api.cache.core.QueryExecutionFactoryCache;
import org.aksw.commons.sparql.api.cache.extra.Cache;
import org.aksw.commons.sparql.api.cache.extra.CacheCore;
import org.aksw.commons.sparql.api.cache.extra.CacheCoreH2;
import org.aksw.commons.sparql.api.cache.extra.CacheImpl;
import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.commons.sparql.api.http.QueryExecutionFactoryHttp;
import org.aksw.commons.sparql.api.model.QueryExecutionFactoryModel;
import org.aksw.commons.sparql.api.pagination.core.QueryExecutionFactoryPaginated;
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

    //@Test
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

    //@Test
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

    //@Test
    public void testPagination() {
        System.out.println("Starting testPagination");

        Model model = ModelFactory.createDefaultModel();
        model.add(RDF.type, RDF.type, RDF.type);
        model.add(RDF.List, RDF.type, RDF.List);

        QueryExecutionFactory f = new QueryExecutionFactoryModel(model);
        

        //QueryExecutionFactory f = createService();
        f = new QueryExecutionFactoryDelay(f, 5000);


        f = new QueryExecutionFactoryPaginated(f, 1);

        QueryExecution q = f.createQueryExecution("Select * {?s ?p ?o}");
        ResultSet rs = q.execSelect();
        while(rs.hasNext()) {
            System.out.println("Here");
            System.out.println(rs.next());
        }

    }

    //@Test
    public void testPaginationComplex() {
        System.out.println("Starting testPagination");

        Model model = ModelFactory.createDefaultModel();
        model.add(RDF.type, RDF.type, RDF.type);
        model.add(RDF.List, RDF.type, RDF.List);

        QueryExecutionFactory f = new QueryExecutionFactoryModel(model);


        //QueryExecutionFactory f = createService();
        //f = new QueryExecutionFactoryDelay(f, 5000);


        f = new QueryExecutionFactoryPaginated(f, 1);

        String queryString = "SELECT ?p (COUNT(?s) AS ?count) WHERE {?s ?p ?o. {SELECT ?s ?o WHERE {?s a ?o.} } }";

        Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);

        QueryExecution q = f.createQueryExecution(queryString);
        ResultSet rs = q.execSelect();
        while(rs.hasNext()) {
            System.out.println("Here");
            System.out.println(rs.next());
        }




        /*
        String query = String.format(queryTemplate, propertyToDescribe, limit, offset);
Map<ObjectProperty, Integer> result = new HashMap<ObjectProperty, Integer>();
ObjectProperty prop;
Integer oldCnt;
boolean repeat = true;
QueryExecutionFactory f = new QueryExecutionFactoryHttp(ks.getEndpoint().getURL().toString(), ks.getEndpoint().getDefaultGraphURIs());
f = new QueryExecutionFactoryPaginated(f, limit);
QueryExecution exec = f.createQueryExecution(QueryFactory.create(query, Syntax.syntaxARQ));
ResultSet rs = exec.execSelect();
int i = 0;
QuerySolution qs;
while(rs.hasNext() && ++i <= maxFetchedRows){
qs = rs.next();
prop = new ObjectProperty(qs.getResource("p").getURI());
int newCnt = qs.getLiteral("count").getInt();
oldCnt = result.get(prop);
if(oldCnt == null){
oldCnt = Integer.valueOf(newCnt);
}
result.put(prop, oldCnt);
qs.getLiteral("count").getInt();
}
*/
    }


    @Test
    public void testHttpDelayCache()
        throws Exception
    {
        QueryExecutionFactory f = createService();

        long delay = 5000;
        f = new QueryExecutionFactoryDelay(f, delay);

        CacheCore core = CacheCoreH2.create("unittest-1");
        Cache cache = new CacheImpl(core);
        f = new QueryExecutionFactoryCache(f, cache);

        ResultSet rs = f.createQueryExecution("Select * {?s ?p ?o .} limit 3").execSelect();
        ResultSetFormatter.outputAsCSV(System.out, rs);

        rs = f.createQueryExecution("Select * {?s ?p ?o .} limit 3").execSelect();
        ResultSetFormatter.outputAsCSV(System.out, rs);
    }

    @Test
    public void testHttpDelayCachePagination() {
        // TBD
    }
}
