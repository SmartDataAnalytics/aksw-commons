package org.aksw.commons.jena;

import com.hp.hpl.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * use Claus ModelUtils
 * @author Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 */

@Deprecated()
public class SaveOntology {
    private static Logger logger = LoggerFactory.getLogger(SaveOntology.class);


    public static void saveAsRDFXML(Model model, String file) {
		write(model, file, Constants.RDFXML);
	}

     public static void saveAsRDF_XML_ABBREV(Model model, String file) {
		write(model, file, Constants.RDF_XML_ABBREV);
	}

     public static void saveAsNTriple(Model model, String file) {
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
            logger.debug("Failed writing to " + file, e);
		}
	}
}
