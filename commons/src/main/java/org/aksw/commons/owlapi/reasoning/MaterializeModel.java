package org.aksw.commons.owlapi.reasoning;

import com.clarkparsia.modularity.PelletIncremantalReasonerFactory;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.aksw.commons.util.Time;
import org.apache.log4j.Logger;
import org.mindswap.pellet.PelletOptions;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class MaterializeModel {
    private static final Logger logger = Logger.getLogger(MaterializeModel.class);

    public static PelletOptions.MonitorType monitorType = PelletOptions.MonitorType.NONE;

    /**
     * NOTE: I decided to modify the inserted ontology object
     *
     * @param ontology
     */
    public static OWLOntology convertToInferredModel(OWLOntology ontology) {

        PelletOptions.USE_CLASSIFICATION_MONITOR = monitorType;

        logger.info(ontology.getAxiomCount());
        Monitor m = MonitorFactory.getTimeMonitor(MaterializeModel.class.getSimpleName() + ".convertToInferredModel").start();

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        // OWLReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ontology);
        OWLReasoner reasoner = PelletIncremantalReasonerFactory.getInstance().createNonBufferingReasoner(ontology);
        // reasoner.
        //reasoner.prepareReasoner();

        List<InferredAxiomGenerator<? extends OWLAxiom>> axiomGenerators = getAxiomGenerators();

        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        for (InferredAxiomGenerator<? extends OWLAxiom> axiomGenerator : axiomGenerators) {
            for (OWLAxiom ax : axiomGenerator.createAxioms(man, reasoner)) {
                changes.add(new AddAxiom(ontology, ax));
            }
        }
        man.applyChanges(changes);

        logger.info("Inference finished " + Time.neededMs(m.stop().getLastValue()));
        logger.info(ontology.getAxiomCount());
        return ontology;
    }

    public static List<InferredAxiomGenerator<? extends OWLAxiom>> getAxiomGenerators() {
        List<InferredAxiomGenerator<? extends OWLAxiom>> axiomGenerators = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
        axiomGenerators.add(new InferredSubClassAxiomGenerator());
        axiomGenerators.add(new InferredPropertyAssertionGenerator());
        axiomGenerators.add(new InferredClassAssertionAxiomGenerator());
        axiomGenerators.add(new InferredDataPropertyCharacteristicAxiomGenerator());
        axiomGenerators.add(new InferredEquivalentClassAxiomGenerator());
        axiomGenerators.add(new InferredEquivalentDataPropertiesAxiomGenerator());
        axiomGenerators.add(new InferredEquivalentObjectPropertyAxiomGenerator());
        axiomGenerators.add(new InferredInverseObjectPropertiesAxiomGenerator());
        axiomGenerators.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
        axiomGenerators.add(new InferredPropertyAssertionGenerator());
        axiomGenerators.add(new InferredSubDataPropertyAxiomGenerator());
        axiomGenerators.add(new InferredSubObjectPropertyAxiomGenerator());
        return axiomGenerators;

    }

}
