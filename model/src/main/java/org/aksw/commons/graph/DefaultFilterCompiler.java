package org.aksw.commons.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.FlatMapView;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

public class DefaultFilterCompiler
		implements IFilterCompiler
{
	@Override
	public List<String> compileFilter(Collection<List<Object>> keys,
			List<String> columnNames)
	{

		List<String> result = new ArrayList<String>();

		// Empty columnNames means that there is no constraint
		if(columnNames.isEmpty())
			return result;
		
		
		// Special handling for a set of resources
		// FIXME right now disabled
		if (columnNames.size() == -1) {

			String columnName = columnNames.get(0);
			Set<Object> remaining = new HashSet<Object>(
					new FlatMapView<Object>(keys));

			Set<Node> resources = new HashSet<Node>();

			Iterator<Object> it = remaining.iterator();
			while (it.hasNext()) {
				Object current = it.next();
				if (current instanceof Node) {
					Node node = (Node)current;
					if(node.isURI()) {					
						resources.add(node);
					}
				}

				it.remove();
			}

			String inPart = Joiner.on(">,<").join(resources);

			if (!inPart.isEmpty()) {
				result.add(columnName + " In (<" + inPart + ">)");
			}

			for (Node resource : resources)
				remaining.remove(resource);

			for (Object o : remaining) {
				result.add(compileFilter((Node)o, columnName));
			}

		} else {

			for (List<Object> key : keys) {
				String part = "";
				for (int i = 0; i < key.size(); ++i) {
					if (!part.isEmpty())
						part += " &&";

					part += compileFilter((Node)key.get(i), columnNames.get(i));
				}

				result.add(part);
			}
		}

		return result;
	}

	private String compileFilter(Node node, String columnName)
	{
		if (node.isURI()) {
			return columnName + " = <" + node.getURI() + ">";
		} else if (node.isLiteral()) {
			String result = "str(" + columnName + ") = \"" + node.getLiteralLexicalForm()
					+ "\"";

			if (!node.getLiteralLanguage().isEmpty()) {
				result += "&& langMatches(lang(" + columnName + "), "
						+ node.getLiteralLanguage() + ")";
			} else if (node.getLiteralDatatype() == null) {
				result += "&& datatype(" + columnName + ") = <"
						+ node.getLiteralDatatypeURI() + ">";
			}

			return result;
		} else {
			throw new RuntimeException(
					"Should never come here - maybe a blank node of evilness?");
		}
	}

}

/**
 * The default filter compiler does not take advantage of some virtuoso specific
 * features, such as the In-Keyword
 * 
 * @author raven
 * 
 */
class DefaultFilterCompilerModel
		implements IFilterCompiler
{
	@Override
	public List<String> compileFilter(Collection<List<Object>> keys,
			List<String> columnNames)
	{

		List<String> result = new ArrayList<String>();

		// Special handling for a set of resources
		if (columnNames.size() == 1) {

			String columnName = columnNames.get(0);
			Set<Object> remaining = new HashSet<Object>(
					new FlatMapView<Object>(keys));

			Set<Resource> resources = new HashSet<Resource>();

			Iterator<Object> it = remaining.iterator();
			while (it.hasNext()) {
				Object current = it.next();
				if (current instanceof Resource) {
					resources.add((Resource) current);
				}

				it.remove();
			}

			String inPart = Joiner.on(">,<").join(resources);

			if (!inPart.isEmpty()) {
				result.add(columnName + " In (<" + inPart + ">)");
			}

			for (Resource resource : resources)
				remaining.remove(resource);

			for (Object o : remaining) {
				result.add(compileFilter(o, columnName));
			}

		} else {

			for (List<Object> key : keys) {
				String part = "";
				for (int i = 0; i < key.size(); ++i) {
					if (!part.isEmpty())
						part += " &&";

					part += compileFilter(key.get(i), columnNames.get(i));
				}

				result.add(part);
			}
		}

		return result;
	}

	private String compileFilter(Object o, String columnName)
	{
		if (o instanceof Resource) {
			return columnName + " = <" + o + ">";
		} else if (o instanceof Literal) {
			Literal l = (Literal) o;

			String result = "str(" + columnName + ") = \"" + l.getLexicalForm()
					+ "\"";

			if (!l.getLanguage().isEmpty()) {
				result += "&& langMatches(lang(" + columnName + "), "
						+ l.getLanguage() + ")";
			} else if (l.getDatatype() == null) {
				result += "&& datatype(" + columnName + ") = <"
						+ l.getDatatypeURI() + ">";
			}

			return result;
		} else {
			throw new RuntimeException(
					"Should never come here - maybe a blank node of evilness?");
		}
	}

}
