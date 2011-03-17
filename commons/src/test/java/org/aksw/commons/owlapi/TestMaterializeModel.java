package org.aksw.commons.owlapi;

import org.aksw.commons.owlapi.reasoning.MaterializeModel;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class TestMaterializeModel {

    @Test
    public void test() {
        try {

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

            List<IRI> iris = new ArrayList<IRI>();
            iris.add(IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl"));
            iris.add(IRI.create(getClass().getClassLoader().getResource("fish.owl").toURI()));
            for (IRI iri : iris) {

                OWLOntology ontology = null;

                ontology = manager.loadOntologyFromOntologyDocument(iri);
                System.out.println("Loaded ontology: " + ontology);

                MaterializeModel.convertToInferredModel(ontology);

            }


        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


}
