package org.aksw.commons.owlapi;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.aksw.commons.util.Time;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 *  @author Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 * Time:  21.09.2010 09:05:42
 */
public class SaveOntology {
    private static final Logger logger = LoggerFactory.getLogger(SaveOntology.class);


    public static void saveOntologyAsRDFXML(OWLOntology ontology, String toFile) {
		saveOntology(ontology, new RDFXMLOntologyFormat(), IRI.create(new File(toFile)));
	}

	public static void saveOntologyAsTurtle(OWLOntology ontology, File toFile) {
		saveOntology(ontology, new TurtleOntologyFormat(), IRI.create(toFile));
	}

	public static void saveOntology(OWLOntology ontology, OWLOntologyFormat format, IRI targetIRI) {
		Monitor m = MonitorFactory.getTimeMonitor(SaveOntology.class.getCanonicalName()+"saveOntology")
				.start();
        try {

			ontology.getOWLOntologyManager().saveOntology(ontology, format, targetIRI);

		} catch (OWLOntologyStorageException e) {
            m.stop();
			logger.error("Could not save ontology to "+targetIRI,e);

		}
		logger.debug("Saving as "+format.toString()+" finished " + Time.neededMs(m.stop().getLastValue()));
	}

    
}
