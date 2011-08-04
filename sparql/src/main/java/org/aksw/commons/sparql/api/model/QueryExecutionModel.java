package org.aksw.commons.sparql.api.model;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.QueryExecutionBase;
import com.hp.hpl.jena.sparql.util.Context;
import org.aksw.commons.sparql.api.core.QueryExecutionFactoryBackQuery;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 8/3/11
 *         Time: 11:32 PM
 * /
public class QueryExecutionModel
    extends QueryExecutionBase {

    private Model model;

    public QueryExecutionModel(Query query, Dataset dataset, Context context, QueryEngineFactory qeFactory) {
        super(query, dataset, context, qeFactory);
    }

    @Override
    public ResultSet execSelect() {
        return QueryExecutionFactory.crea
    }

}
*/