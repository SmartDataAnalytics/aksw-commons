package org.aksw.commons.sparql.api.dereference;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.*;
import org.aksw.commons.sparql.api.core.QueryExecutionAdapter;
import org.aksw.commons.sparql.api.core.QueryExecutionBaseSelect;
import org.deri.any23.Any23;
import org.deri.any23.extractor.ExtractionContext;
import org.deri.any23.extractor.ExtractionException;
import org.deri.any23.writer.TripleHandler;
import org.deri.any23.writer.TripleHandlerException;
import org.deri.any23.http.HTTPClient;
import org.deri.any23.source.DocumentSource;
import org.deri.any23.source.HTTPDocumentSource;
import org.deri.any23.writer.TripleHandlerException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import javax.xml.transform.Result;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 *
 */
class MyTripleHandler implements TripleHandler {
    private Model model;

    public MyTripleHandler(Model model) {
        this.model = model;
    }


	public void startDocument(URI documentURI) throws TripleHandlerException {
	}

	public void openContext(ExtractionContext context) throws TripleHandlerException {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.deri.any23.writer.TripleHandler#receiveTriple(org.openrdf.model.Resource
	 * , org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.URI,
	 * org.deri.any23.extractor.ExtractionContext)
	 */
	public void receiveTriple(org.openrdf.model.Resource s, URI p, Value o, URI g, ExtractionContext context)
			throws TripleHandlerException {

		if (o instanceof org.openrdf.model.Resource && !(o instanceof org.openrdf.model.BNode)
				&& s instanceof org.openrdf.model.Resource && !(s instanceof org.openrdf.model.BNode)) {
			org.openrdf.model.Resource r = (org.openrdf.model.Resource) o;

            Resource jenaS = model.createResource(s.stringValue());
			Property jenaP = model.createProperty(p.stringValue());
			RDFNode jenaO = model.createResource(r.stringValue());

            model.add(jenaS, jenaP, jenaO);
            /*
			// Add outgoing triple
			if (s.toString().equals(resource.getURI())
					&& (direction.equals(Direction.OUT) || direction.equals(Direction.BOTH)))
				buffer.add(model.createStatement(jenaS, jenaP, jenaO));

			// Add incoming triple
			if (o.toString().equals(resource.getURI())
					&& (direction.equals(Direction.IN) || direction.equals(Direction.BOTH)))
				buffer.add(model.createStatement(jenaS, jenaP, jenaO));
		    */
		}
	}

	public void receiveNamespace(String prefix, String uri, ExtractionContext context) throws TripleHandlerException {
	}

	public void closeContext(ExtractionContext context) throws TripleHandlerException {
	}

	public void endDocument(URI documentURI) throws TripleHandlerException {
	}

	public void setContentLength(long contentLength) {
	}

	public void close() throws TripleHandlerException {
	}
}


/**
 * A QueryExecution which dereferences URIs on the Web using the Any23 library.
 * 
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/29/11
 *         Time: 12:08 AM
 */
public class QueryExecutionDereferenceOld
    extends QueryExecutionAdapter
{
    private Query query;
    private MyTripleHandler tripleHandler;
    private Any23 runner;

    public QueryExecutionDereferenceOld(Query query, Any23 runner)
    {
        this.query = query;
        this.runner = runner;
        this.tripleHandler = null;
    }

    @Override
    public Model execDescribe() {
        try {
            return _execDescribe(ModelFactory.createDefaultModel());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Model execDescribe(Model model) {
        try {
            Model result = _execDescribe(model);

            if(model != result) {
                result.add(model);
            }

            return result;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Model _execDescribe(Model model)
            throws IOException, URISyntaxException, ExtractionException {
        Node node = QueryExecutionBaseSelect.extractDescribeNode(query);

        timeoutHelper.startExecutionTimer();
        tripleHandler = new MyTripleHandler(model);


        //Any23 runner = new Any23();
        //runner.setHTTPUserAgent("LATC QA tool <c.d.m.gueret@vu.nl>");
        HTTPClient httpClient = runner.getHTTPClient();


        DocumentSource source = new HTTPDocumentSource(httpClient, node.getURI());
        runner.extract(source, tripleHandler);

        timeoutHelper.startExecutionTimer();

        return model;
    }


    @Override
    public void close() {
        synchronized (this) {
            if(runner != null) {
                try {
                    runner.getHTTPClient().close();
                    runner = null;
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
