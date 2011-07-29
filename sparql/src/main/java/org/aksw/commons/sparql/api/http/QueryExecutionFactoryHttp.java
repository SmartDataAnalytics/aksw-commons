package org.aksw.commons.sparql.api.http;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import org.aksw.commons.sparql.api.core.QueryExecutionFactoryBackString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 9:47 PM
 */
public class QueryExecutionFactoryHttp
    extends QueryExecutionFactoryBackString
{
    private String service;

    // Note: TreeSet ensures fixed order of the graphs
    private List<String> defaultGraphs = new ArrayList<String>();

    public QueryExecutionFactoryHttp(String service, Collection<String> defaultGraphs) {
        this.service = service;
        this.defaultGraphs = new ArrayList<String>(defaultGraphs);
        Collections.sort(this.defaultGraphs);;
    }

    @Override
    public String getId() {
        return service;
    }

    @Override
    public String getState() {
        return Joiner.on("|").join(defaultGraphs);
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        QueryEngineHTTP engine = new QueryEngineHTTP(service, queryString);
        engine.setDefaultGraphURIs(defaultGraphs);

        return engine;
    }
}
