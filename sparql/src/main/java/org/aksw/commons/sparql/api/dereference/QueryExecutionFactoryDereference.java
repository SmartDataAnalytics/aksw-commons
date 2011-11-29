package org.aksw.commons.sparql.api.dereference;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import org.aksw.commons.sparql.api.core.QueryExecutionFactoryBackQuery;
import org.deri.any23.Any23;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/29/11
 *         Time: 12:42 AM
 */
public class QueryExecutionFactoryDereference
    extends QueryExecutionFactoryBackQuery<QueryExecution>
{
    public String userAgent = null;

    public QueryExecutionFactoryDereference(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public String getId() {
        return "http://aksw.org/commons/TheInternet";
    }

    @Override
    public String getState() {
        return "";
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        Any23 runner = new Any23();

        if(userAgent != null) {
            runner.setHTTPUserAgent(userAgent);
        }

        return new QueryExecutionDereference(query, runner);
    }
}
