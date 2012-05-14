package org.aksw.commons.jena;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * @author Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 */
public interface OntologyLoader {


    public OntModel loadOntology(String ontologyUri) ;

    /**
     * Recursively loads any imported models. They will be included as Jena submodels
     * @param model
     */
    public void loadImports(OntModel model);
}
