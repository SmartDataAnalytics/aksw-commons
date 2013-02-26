package org.aksw.commons.owlapi.reasoning.impl;

import com.clarkparsia.modularity.IncrementalClassifier;
import com.clarkparsia.modularity.PelletIncremantalReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class PelletIncrementalClassifier implements org.aksw.commons.owlapi.reasoning.IncrementalClassifier {
    private static final Logger logger = LoggerFactory.getLogger(PelletIncrementalClassifier.class);

    private OWLDataFactory factory;
    private OWLOntologyManager manager;
    private OWLOntology ontology;
    private IncrementalClassifier incReasoner;

    Set<OWLAxiom> previouslyAddedAxioms = new HashSet<OWLAxiom>();

    private boolean getSuperClasses = true;
    private boolean getEquivalentClasses = false;
    private boolean getSubClasses = true;
    private boolean getParallelClasses = false;


    public PelletIncrementalClassifier(OWLOntology ontology, IncrementalClassifier incReasoner) {
        this.ontology = ontology;
        manager = ontology.getOWLOntologyManager();
        factory = manager.getOWLDataFactory();
        this.incReasoner = incReasoner;
        incReasoner.prepareReasoner();
        logger.debug("Successfully initialized reasoner");
    }

    public static PelletIncrementalClassifier getInstance(OWLOntology ontology) {
        IncrementalClassifier inc = PelletIncremantalReasonerFactory.getInstance().createNonBufferingReasoner(ontology);
        return new PelletIncrementalClassifier(ontology, inc);
    }

    public static PelletIncrementalClassifier getInstance(IRI iri) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntology(iri);
        return getInstance(ontology);
    }

    public SubsumptionResult getSubsumptionResult(String classUri, boolean direct) {
        OWLClass cl = factory.getOWLClass(IRI.create(classUri));
        return getSubsumptionResult(cl, direct);

    }

    public SubsumptionResult getSubsumptionResult(OWLClass cl, boolean direct) {
        SubsumptionResult sr = new SubsumptionResult();

        if (getSuperClasses) sr.superClasses = incReasoner.getSuperClasses(cl, direct).getFlattened();
        if (getSubClasses) sr.subClasses = incReasoner.getSuperClasses(cl, direct).getFlattened();
        //this is based on the assumption that super and sub are true
        if (getEquivalentClasses) {
            sr.equivalentClasses = new HashSet<OWLClass>(sr.subClasses);
            sr.equivalentClasses.retainAll(sr.superClasses);
        }
        //TODO 
        if (getParallelClasses) {
            sr.superClasses = incReasoner.getSuperClasses(cl, direct).getFlattened();
        }

        logger.debug("Found for " + cl.toString() + "\n" + sr);
        return sr;
    }

    public void addAxioms(Set<OWLAxiom> axioms) {
        previouslyAddedAxioms.addAll(axioms);
        Set<OWLAxiomChange> s = new HashSet<OWLAxiomChange>();
        for (OWLAxiom ax : axioms) {
            s.add(new AddAxiom(ontology, ax));
        }
        change(s);

    }


    public void removeAxioms(Set<OWLAxiom> axioms) {
        previouslyAddedAxioms.removeAll(axioms);
        Set<OWLAxiomChange> s = new HashSet<OWLAxiomChange>();
        for (OWLAxiom ax : axioms) {
            s.add(new RemoveAxiom(ontology, ax));
        }
        change(s);
    }

    private void change(Set<OWLAxiomChange> axioms) {
        int before = ontology.getAxiomCount();
        for (OWLAxiomChange owlAxiom : axioms) {
            manager.applyChange(owlAxiom);
            if (owlAxiom instanceof AddAxiom) {
                logger.debug("Added axiom " + owlAxiom.toString());
            } else if (owlAxiom instanceof RemoveAxiom) {
                logger.debug("Removed axiom " + owlAxiom.toString());
            }
        }
        incReasoner.prepareReasoner();
        validate(before, ontology.getAxiomCount(), axioms.size());

    }


    public void removeAllpreviouslyAddedAxioms() {
        removeAxioms(previouslyAddedAxioms);
        previouslyAddedAxioms = new HashSet<OWLAxiom>();
    }

    private void validate(int before, int after, int expectedDifference) {
        if (!(Math.abs((before - after)) == expectedDifference)) {
            String message = "adding/removing axioms failed:\n";
            message += "before:" + before + "\n";
            message += "after:" + after + "\n";
            message += "expected difference:" + expectedDifference + "\n";
            logger.error(message);
        }
    }

}
