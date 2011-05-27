package org.aksw.commons.jena.impl;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.aksw.commons.jena.OntologyLoader;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 */
public class SimpleLoader implements OntologyLoader {
    private static final Logger log = Logger.getLogger(SimpleLoader.class);

    @Override
    public OntModel loadOntology(String ontologyUri) {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelFactory.createDefaultModel());
        model.read(ontologyUri);
        return model;
    }

    @Override
    public void loadImports(OntModel m) {
        Set<String> loaded = new HashSet<String>();
        for (String one : m.listImportedOntologyURIs()) {
            if (loaded.add(one) == false) {
                log.debug("skipping " + one + " (already load)");
                continue;
            }
            OntModel sm = loadOntology(one);
            loadImports(sm);
            m.addSubModel(sm);
        }
    }
}
