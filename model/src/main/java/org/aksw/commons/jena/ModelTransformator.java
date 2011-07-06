package org.aksw.commons.jena;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Statement;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class ModelTransformator {
    private static final Logger logger = Logger.getLogger(ModelTransformator.class);

     public static void separateLiterals(OntModel original, OntModel literals, OntModel rest) {
        List<Statement> statements = original.listStatements().toList();
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.getObject().isLiteral()) {
                literals.add(statement);
            } else {
                rest.add(statement);
            }
        }
         logger.debug(original.size()+" triples were split into "+literals.size()+" literals and "+rest.size()+" rest");
    }
}
