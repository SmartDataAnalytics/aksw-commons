package org.aksw.commons.jena_owlapi;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import org.aksw.commons.jena.Constants;
import org.aksw.commons.jena.StringConverter;
import org.apache.log4j.Logger;
import org.coode.owlapi.rdf.model.RDFGraph;
import org.coode.owlapi.rdf.model.RDFLiteralNode;
import org.coode.owlapi.rdf.model.RDFNode;
import org.coode.owlapi.rdf.model.RDFTriple;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.util.Collections;
import java.util.Set;

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


    public static Set<RDFTriple> extractTriples(RDFGraph graph)
    {
        Field field = null;
        try {
            field = graph.getClass().getDeclaredField("triples");

            boolean saved = field.isAccessible();
            field.setAccessible(true);

            Set<RDFTriple> triples = (Set<RDFTriple>)field.get(graph);
            field.setAccessible(saved);

            return triples;
        } catch (Exception e) {
            throw new RuntimeException("Should not happen", e);
        }
    }

    public static String toStringNTriples(RDFGraph graph) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writeNTriples(graph, out);

        return out.toString();
    }

    public static void writeNTriples(RDFGraph graph, OutputStream out)
    {
        writeNTriples(graph, new PrintStream(out));
    }

    public static void writeNTriples(RDFGraph graph, PrintStream out)
    {
        Set<RDFTriple> triples = extractTriples(graph);

        for(RDFTriple triple : triples) {
           out.println(toStringNTriples(triple));
        }
    }

    public static String toStringNTriples(RDFTriple triple)
    {
        return toStringSparql(triple) + " .";
    }

    public static String toStringSparql(RDFTriple triple)
	{
		return
			toString(triple.getSubject()) + " " +
			toString(triple.getProperty()) + " " +
			toString(triple.getObject());
	}

	public static String toString(RDFNode node)
	{
		if(node.isAnonymous())
			return "_:a" + node.toString();
		else if(node.isLiteral()) {
			RDFLiteralNode n = (RDFLiteralNode)node;

			String literal = sparqlEscapeLiteral(n.getLiteral());
			String result = "\"\"\"" + literal + "\"\"\"";

			if(n.getLang() != null && !n.getLang().isEmpty())
				result += "@" + n.getLang().toLowerCase();

			if(n.getDatatype() != null) {
				result += "^^" + "<" + n.getDatatype() + ">";
            }

			return result;
		}
		else { // resource
			return  node.toString();
        }
	}

    public static String sparqlEscapeLiteral(String literal)
    {
        return literal.replace("\"", "\\\"");
    }

}
