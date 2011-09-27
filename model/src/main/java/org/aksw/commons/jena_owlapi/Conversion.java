package org.aksw.commons.jena_owlapi;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.aksw.commons.jena.Constants;
import org.aksw.commons.jena.StringConverter;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.ByteArrayInputStream;

/**
 * Converts between Jena and OWLApi models
 */
public class Conversion {
	private static final Logger logger = Logger.getLogger(Conversion.class);

	public static OWLOntology JenaModel2OWLAPIOntology(Model m){
        String rdfxml = new StringConverter(m).toStringAsRDFXML();

        ByteArrayInputStream bs = new ByteArrayInputStream(rdfxml.getBytes());
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology retOnt = null;
		try {
			retOnt = manager.loadOntologyFromOntologyDocument(bs);
		} catch (OWLOntologyCreationException e) {
			logger.error("could not create ontology ",e);
		}
        return retOnt;

    }

    /**
     *
     * @param ontology
     * @return a OntModelSpec.OWL_DL_MEM Model
     */
    public static OntModel OWLAPIOntology2JenaOntModel(OWLOntology ontology){
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        return (OntModel) OWLAPIOntology2JenaModel(ontology, m);

    }

    /**
     *
     * @param ontology
     * @param resultModel the Model that will be filled
     * @return
     */
    public static Model OWLAPIOntology2JenaModel(OWLOntology ontology, Model resultModel){
        String rdfxml = new org.aksw.commons.owlapi.StringConverter(ontology).toStringAsRDFXML();
        ByteArrayInputStream bs = new ByteArrayInputStream(rdfxml.getBytes());
        resultModel.read(bs, "", Constants.RDFXML);
        return resultModel;

    }


}
