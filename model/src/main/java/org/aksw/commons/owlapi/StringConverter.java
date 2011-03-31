package org.aksw.commons.owlapi;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.aksw.commons.util.Time;
import org.apache.log4j.Logger;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 * Time:  21.09.2010 09:03:30
 */
public class StringConverter {
    private static final Logger logger = Logger.getLogger(StringConverter.class);

    final private OWLOntology ontology;

    public StringConverter(OWLOntology ontology) {
        this.ontology = ontology;
    }

    /**
     * Defaults to turtle
     * @return
     */
    @Override
    public String toString(){
        return toStringAsTurtle();
    }

    public String toStringAsTurtle() {
		return toString(new TurtleOntologyFormat());
	}

    public String toStringAsRDFXML() {
		return toString(new RDFXMLOntologyFormat());
	}

    public String toStringAsManchesterOWLSyntax() {
		return toString(new ManchesterOWLSyntaxOntologyFormat());
	}

    public String toStringAsManchesterOWLSyntax( Map<String, String> prefixToNamespaceMap) {

        ManchesterOWLSyntaxOntologyFormat format = new ManchesterOWLSyntaxOntologyFormat();
        for (String key : prefixToNamespaceMap.keySet()) {
            String value = prefixToNamespaceMap.get(key);
            format.setPrefix(value, key);
        }
        String ret =  toString(format);
        return ret;

	}

    public String toString( OWLOntologyFormat format) {
        Monitor m = MonitorFactory.getTimeMonitor(StringConverter.class.getCanonicalName() + "toString").start();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ontology.getOWLOntologyManager().saveOntology(ontology, format, baos);
        } catch (OWLOntologyStorageException e) {
            m.stop();
            logger.error("Could not convert ontology to string"+format.toString(), e );
        }
        String ret = baos.toString();
        logger.debug("Conversion of OWLAPI to "+format.toString()+" finished ["+ret.length()+" chars] " + Time.neededMs(m.stop().getLastValue()));
        return ret;
    }

    /**
     * tries to give the best possible representation for humans
     * currently removes all extra lines and the prefixes from the MOS string
     * @return
     */
    public String toStringHumanReadable(Map<String, String> prefixMap){
        String mos = toStringAsManchesterOWLSyntax(prefixMap);
        mos = mos.replaceAll("\n\n", "\n").replaceAll("\n\n", "\n");
        StringBuffer buffer = new StringBuffer();
        for (String m : mos.split("\n")) {
            String current = m.trim();
            if (current.length() == 0 || m.startsWith("Prefix:")) {
                continue;
            }
            buffer.append(m);
            buffer.append("\n");
        }
        mos = buffer.toString();
        return mos;
    }


}
