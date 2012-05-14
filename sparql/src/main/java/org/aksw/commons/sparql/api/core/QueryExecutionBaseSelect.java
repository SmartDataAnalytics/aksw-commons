package org.aksw.commons.sparql.api.core;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.update.UpdateRequest;
import org.aksw.commons.collections.IClosable;
import org.aksw.commons.collections.PrefetchIterator;
import org.aksw.commons.jena.util.QueryUtils;
import org.aksw.commons.sparql.api.util.CannedQueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


class TestQueryExecutionBaseSelect
    extends QueryExecutionBaseSelect
{

    public TestQueryExecutionBaseSelect(Query query) {
        super(query, null);
    }

    @Override
    protected QueryExecution executeCoreSelectX(Query query) {
        System.out.println("Got a query string: " + query);
        return null;
    }

    public static void main(String[] args) {
        Query query = QueryFactory.create("Describe ?x <http://aaaa> {?x a <http://blah> .}");
        query = QueryFactory.create("Describe <http://aaaa>");
        query = QueryFactory.create("Describe");
        TestQueryExecutionBaseSelect x = new TestQueryExecutionBaseSelect(query);

        x.execDescribe();

    }
}


/**
 *
 *
 * TODO What about Limit and Offset?
 *
 */
class Describer
    extends PrefetchIterator<Triple> {

    private Iterator<Node> openNodes;
    private Collection<Var> resultVars;

    //private QueryExe
    // TODO Keep track of the involved resources so we can close them properly

    private ResultSet rs;
    private Binding currentBinding = null;
    private Iterator<Var> currentVar = null;
    private QueryExecutionFactory<QueryExecutionStreaming> qef;


    public Describer(Iterator<Node> openNodes, ResultSet rs, Collection<Var> resultVars, QueryExecutionFactory<QueryExecutionStreaming> qef)
    {
        this.openNodes = openNodes;
        this.resultVars = resultVars;
        this.rs = rs;
        this.qef = qef;
    }

    public static Describer create(List<Node> resultUris, List<String> resultVars, ResultSet rs, QueryExecutionFactory<QueryExecutionStreaming> qef) {

        Set<Var> vars = null;
        if(rs != null) {
            if(!rs.hasNext()) {
                rs = null;
                resultVars = null;
            } else {
                vars = new HashSet<Var>();

                for(String var : resultVars) {
                    vars.add(Var.alloc(var));
                }
            }
        }

        Iterator<Node> it = (resultUris == null)
                ? null
                : resultUris.iterator();

        return new Describer(it, rs, vars, qef);
    }


    public Iterator<Triple> describeNodeStreaming(Node node) {
        Query query = CannedQueryUtils.constructBySubject(node);

        QueryExecutionStreaming qe = qef.createQueryExecution(query);

        return qe.execConstructStreaming();
    }

    @Override
    protected Iterator<Triple> prefetch() throws Exception {
        Set<Node> batch = new HashSet<Node>();

        int n = 10;
        while(openNodes != null && openNodes.hasNext() && batch.size() < n) {
            Node node = openNodes.next();
            batch.add(node);
        }


        while(batch.size() < n) {

            if(rs == null) {
                break;
            }


            if(currentVar == null || !currentVar.hasNext()) {

                if(!rs.hasNext()) {
                    break;
                }

                currentBinding = rs.nextBinding();

                currentVar = resultVars.iterator();
            }

            Var var = currentVar.next();
            Node node = currentBinding.get(var);

            if(node == null || !node.isURI()) {
                continue;
            }

            batch.add(node);
        }

        if(batch.isEmpty()) {
            return null;
        }

        Query q = CannedQueryUtils.constructBySubjects(batch);
        QueryExecutionStreaming qe = qef.createQueryExecution(q);

        return qe.execConstructStreaming();
    }
}


/**
 * A Sparqler-class that implements ask, describe, and construct
 * based on the executeCoreSelect(Query) method.
 * 
 * Also, works on String and Query level.
 * 
 * Some of the code has been taken from 
 * com.hp.hpl.jena.sparql.engine.QueryExecutionBase, which is a
 * class with a similar purpose but not as reusable as this one
 * (This class reduces all operations to a single executeCoreSelect call)
 *
 * NOTE: executeCoreSelect will close this query execution once the ResultSet is consumed.
 *
 * @author raven
 *
 */
public abstract class QueryExecutionBaseSelect
        extends QueryExecutionDecorator
        implements QueryExecutionStreaming
{
	private static final Logger logger = LoggerFactory
			.getLogger(QueryExecutionBaseSelect.class);

    private Query query;


    // Describe queries are sent as multiple individual queries, therefore we require a
    // reference to a QueryExecutionFactory
    private QueryExecutionFactory<QueryExecutionStreaming> subFactory;


    // TODO Move these two utility methods to a utility class
    // Either the whole Sparql API should go to the jena module
    // or it needs a dependency on that module...
    public static Model createModel(Iterator<Triple> it) {
        return createModel(ModelFactory.createDefaultModel(), it);
    }

    public static Model createModel(Model result, Iterator<Triple> it) {

        while(it.hasNext()) {
            Triple t = it.next();
            Statement stmt = com.hp.hpl.jena.sparql.util.ModelUtils.tripleToStatement(result, t);
            if (stmt != null) {
                result.add(stmt);
            }
        }

        return result;
    }



    public QueryExecutionBaseSelect(Query query, QueryExecutionFactory<QueryExecutionStreaming> subFactory) {
        super(null);
        this.query = query;
        this.subFactory = subFactory;
    }

    //private QueryExecution running = null;

	abstract protected QueryExecution executeCoreSelectX(Query query);
	
    protected ResultSet executeCoreSelect(Query query) {
        if(this.decoratee != null) {
            throw new RuntimeException("A query is already running");
        }

        this.decoratee = executeCoreSelectX(query);

        if(this.decoratee == null) {
            throw new RuntimeException("Failed to obtain a QueryExecution for query: " + query);
        }

        //return decoratee.execSelect();

        ResultSet tmp = decoratee.execSelect();
        final QueryExecution self = this;
        ResultSetClosing result = new ResultSetClosing(tmp, new IClosable() {
            @Override
            public void close() {
                decoratee.close();
                self.close();
            }
        });

        return result;

        
    }

	@Override
	public boolean execAsk() {
		if (!query.isAskType()) {
			throw new RuntimeException("ASK query expected. Got: ["
					+ query.toString() + "]");
		}

		Query selectQuery = QueryUtils.elementToQuery(query.getQueryPattern());
		selectQuery.setLimit(1);

		ResultSet rs = executeCoreSelect(selectQuery);

		long rowCount = 0;
		while(rs.hasNext()) {
			++rowCount;
		}

		if (rowCount > 1) {
			logger.warn("Received " + rowCount + " rows for the query ["
					+ query.toString() + "]");
		}

		return rowCount > 0;
	}

    @Override
    public Model execDescribe() {
        return execDescribe(ModelFactory.createDefaultModel());
    }


    public static Node extractDescribeNode(Query query) {
        if (!query.isDescribeType()) {
            throw new RuntimeException("DESCRIBE query expected. Got: ["
                    + query.toString() + "]");
        }

        // TODO Right now we only support describe with a single constant.

        Element queryPattern = query.getQueryPattern();
        if(query.getQueryPattern() != null || !query.getResultVars().isEmpty() || query.getResultURIs().size() > 1) {
            throw new RuntimeException("Sorry, DESCRIBE is only implemented for a single resource argument");
        }

        Node result = query.getResultURIs().get(0);

        return result;
    }


    /**
     * We use this query execution for retrieving the result set of the
     * where clause, but we neet the subFactory to describe the individual
     * resources then.
     *
     * @return
     */
    @Override
    public Iterator<Triple> execDescribeStreaming() {

        ResultSet rs = null;
        if ( query.getQueryPattern() != null ) {
            Query q = new Query();
            q.setQuerySelectType();
            q.setResultVars();
            for(String v : query.getResultVars()) {
                q.addResultVar(v);
            }
            q.setQueryPattern(query.getQueryPattern());

            rs = this.executeCoreSelect(q);
        }

        Describer result = Describer.create(query.getResultURIs(), query.getResultVars(), rs, subFactory);
        return result;
    }

    /**
     * A describe query is translated into a construct query.
     *
     *
     *
     * Lets see...
     * Describe ?a ?b ... &lt;x&gt;&lt;y&gt; Where Pattern { ... } becomes ...?
     *
     * Construct { ?a ?ap ?ao . ?b ?bp ?bo . } Where Pattern {  } Union {}
     * Ah, lets just query every resource individually for now
     *
     *
     * TODO Add support for concise bounded descriptions...
     *
     * @param result
     * @return
     */
	@Override
	public Model execDescribe(Model result) {
        createModel(result, execDescribeStreaming());
        return result;


        /*
        Generator generator = Gensym.create("xx_generated_var_");

        Element queryPattern = query.getQueryPattern();
        ElementPathBlock pathBlock;

        if(queryPattern == null) {
            ElementGroup elementGroup = new ElementGroup();

            pathBlock = new ElementPathBlock();
            elementGroup.addElement(pathBlock);
        } else {

            ElementGroup elementGroup = (ElementGroup)queryPattern;

            pathBlock = (ElementPathBlock)elementGroup.getElements().get(0);
        }

        //Template template = new Template();
        //template.

        BasicPattern basicPattern = new BasicPattern();

        System.out.println(queryPattern.getClass());

        for(Node node : query.getResultURIs()) {
            Var p = Var.alloc(generator.next());
            Var o = Var.alloc(generator.next());

            Triple triple = new Triple(node, p, o);

            basicPattern.add();
            //queryPattern.
        }

        for(String var : query.getResultVars()) {

        }


        Template template = new Template(basicPattern);


        Query selectQuery = QueryUtils.elementToQuery(query.getQueryPattern());

        ResultSet rs = executeCoreSelect(selectQuery);


		//throw new RuntimeException("Sorry, DESCRIBE is not implemted yet.");
		*/
	}

    private Iterator<Triple> executeConstructStreaming(Query query) {
        if (!query.isConstructType()) {
            throw new RuntimeException("CONSTRUCT query expected. Got: ["
                    + query.toString() + "]");
        }

        //Query selectQuery = QueryUtils.elementToQuery(query.getQueryPattern());
        query.setQueryResultStar(true);
        ResultSet rs = executeCoreSelect(query);

        // insertPrefixesInto(result) ;
        Template template = query.getConstructTemplate();

        return new ConstructIterator(template, rs);
    }

    private Model executeConstruct(Query query, Model result) {
        createModel(result, executeConstructStreaming(query));
        return result;
    }

	@Override
	public Model execConstruct(Model result) {
		return executeConstruct(this.query, result);
	}

    @Override
    public Iterator<Triple> execConstructStreaming() {
        return executeConstructStreaming(this.query);
    }

	@Override
	public ResultSet execSelect() {
		if (!query.isSelectType()) {
			throw new RuntimeException("SELECT query expected. Got: ["
					+ query.toString() + "]");
		}
		
		return executeCoreSelect(query);
	}
	
	//@Override
	public void executeUpdate(UpdateRequest updateRequest)
	{
		throw new RuntimeException("Not implemented");
	}
}
