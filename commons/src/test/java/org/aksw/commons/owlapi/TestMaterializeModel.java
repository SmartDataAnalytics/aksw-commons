package org.aksw.commons.owlapi;

import org.aksw.commons.owlapi.reasoning.MaterializeModel;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.net.URISyntaxException;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class TestMaterializeModel {

    @Test
    public void testPizza() {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

            IRI iri = IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl");
            OWLOntology pizzaOntology = null;

            pizzaOntology = manager.loadOntologyFromOntologyDocument(iri);
            System.out.println("Loaded ontology: " + pizzaOntology);

            MaterializeModel.convertToInferredModel(pizzaOntology);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

     @Test
    public void testFish() {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

            IRI iri = IRI.create(getClass().getClassLoader().getResource("fish.owl").toURI());
            OWLOntology pizzaOntology = null;

            pizzaOntology = manager.loadOntologyFromOntologyDocument(iri);
            System.out.println("Loaded ontology: " + pizzaOntology);

            MaterializeModel.convertToInferredModel(pizzaOntology);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }
}
