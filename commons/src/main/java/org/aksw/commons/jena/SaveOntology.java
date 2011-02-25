package org.aksw.commons.jena;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;

/**
 * As simple Helper to save ontologies
 * Created by IntelliJ IDEA.
 * User: Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 * Time:  20.09.2010 18:18:11
 */
public class SaveOntology {
    private static final Logger logger = Logger.getLogger(SaveOntology.class);
    

    public static void saveAsRDFXML(Model model, String file) {
		write(model, file, Constants.RDFXML);
	}

     public static void saveAsRDF_XML_ABBREV(Model model, String file) {
		write(model, file, Constants.RDF_XML_ABBREV);
	}

     public static void saveAsNTripleL(Model model, String file) {
		write(model, file, Constants.N_TRIPLE);
	}

	public static void saveAsN3(Model model, String file) {
		write(model, file, Constants.N3);
	}

    public static void saveAsTurtle(Model model, String file) {
		write(model, file, Constants.TURTLE);
	}

    public static void saveAsTTL(Model model, String file) {
		write(model, file, Constants.TTL);
	}

	public static void write(Model m, String file, String format) {
		try {
			m.write(new FileWriter(file), format);
			logger.debug("Model written to " + file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
