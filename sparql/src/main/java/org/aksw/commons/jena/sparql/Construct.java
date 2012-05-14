package org.aksw.commons.jena.sparql;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class Construct {
    private static final Logger logger = LoggerFactory.getLogger(Construct.class);


    /**
     * @param model           the model to execute the result on
     * @param sparqlConstruct duh
     * @param resultModel     the model to load the result into
     * @return
     */
    public static OntModel execute(OntModel model, String sparqlConstruct, OntModel resultModel) {
        logger.trace("Construct query\n" + sparqlConstruct);
        Query query = QueryFactory.create(sparqlConstruct);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        try {
            qexec.execConstruct(resultModel);
        } finally {
            qexec.close();
        }
        return resultModel;
    }

    /**
     * @param model
     * @param sparqlConstruct
     * @return a new and filled Model with OntModelSpec.OWL_DL_MEM
     */
    public static OntModel execute(OntModel model, String sparqlConstruct) {
        OntModel resultModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelFactory.createDefaultModel());
        return execute(model,sparqlConstruct, resultModel);
    }
}
