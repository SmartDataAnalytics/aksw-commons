package org.aksw.commons.owlapi.reasoning;

import com.clarkparsia.modularity.PelletIncremantalReasonerFactory;
import org.aksw.commons.owlapi.reasoning.impl.PelletIncrementalClassifier;
import org.aksw.commons.owlapi.reasoning.impl.SubsumptionResult;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public interface IncrementalClassifier {

    public SubsumptionResult getSubsumptionResult(String classUri, boolean direct);
    

}
