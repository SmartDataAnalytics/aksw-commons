package org.aksw.commons.graph;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.base.Joiner;
import org.aksw.commons.collections.CacheSet;
import org.aksw.commons.collections.CollectionUtils;
import org.aksw.commons.collections.diff.ModelDiff;
import org.aksw.commons.collections.random.RandomUtils;
import org.aksw.commons.util.strings.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;


/**
 * This file is a playground for my thoughts of what a
 * generic cache implementation
 * could look like.
 * 
 * @author raven
 *
 */


/**
 * Potential problem: Checking whether the baseGraph already contains certain triples 
 * would ideally require a cache.
 * 
 * Actually, maybe this is not a problem at all:
 * 
 * Whenever a triple is added, the caches are informed.
 * Every (in)complete partition in the cache is then updated accordingly.
 * Duplicate triples will have no effect.
 * The same goes for removed triples (removing a triple will remove the triple
 * from the index partitions, but the partitions will retain their completion state).
 * 
 * As a consequence, a find on these partitions will work on the cache only.
 *
 * AH! Here is the problem: As soon as the delta gets cleared, we
 * need to know whether some triples must remain cached, since they are
 * contained in the baseGraph (but no longer in the delta).
 * 
 * One thing is, that as soon as a diff is applied, the triples are added to the
 * base graph, and the base graph fires the respective events.
 * 
 *  
 * One of the questions is, on which level to do that union/intersection
 * thing: On cacheindex level? Then we need a view over the maps.
 * And this seems rather complicated (as the cacheindexes must be aware of
 * underlying structure).
 * 
 * 
 * So maybe it is good enough to do it this way:
 * Whenever a bulkfind is executed on the base graph, we get back a set
 * of triples. We can cross check these triples whether they appear in the
 * delta and flag them accordingly.
 * 
 * However, the disadvantage is, that triples may be added/removed after a bulkfind
 * whereas these triples appeared in the bulkfind.
 * Although the indexes would know the triples, we are on a lower level where
 * this information is not easily available.
 * Of course we could introduce an extra cache - but the point is, that triples
 * invalidated by high level caches aren't needed on the lower level.
 * So triples may be invalidated on the high level, which means that won´ t be
 * required anymore, but we wouldn't take note of this on the lower level and
 * still keep them in a cache.
 * 
 * 
 * 
 * 
 *  
 * 
 * 
 * @author raven
 *
 */








/**
 * A triple cache only indexes single triple in accordance with a specified
 * filter.
 * 
 * 
 * Please take note of the following points:
 * 
 * . The cache acts merely as a filter and a proxy to its indexes.
 *   So the indexes only receive triples that pass the filter.
 *   You must attach at least one CacheIndex 
 * 
 * . The cache is incapable of caching graph patterns
 *   which would require a join of triple patterns.
 *   However, in future the triple cache may be serve as a base for a
 *   TriplePatternCache.
 * 
 * . The triple cache is based on sparql over jena models.
 *   This means, that testing individual triples against the filter is very slow.
 *   Therefore, always try to use batch inserts/removals.  
 * 
 * . The indexes maintain a map of key-values internally.
 *   These key-value mappings are transformed into a model on demand.
 * 
 * 
 * ------
 * Following point is outdated
 * . Statements may have references to the model that created them.
 *   This means that if the cache contains triples of multiple temporary models
 *   these models could not be cleaned up.
 *   Therefore the design decision was for each
 *   TripleCache instance to maintain its own model.
 *   
 *   
 * 
 * @author raven
 *
 */




/**
 * An index for a TripleCache.
 * 
 * The index registers itself automatically at the supplied cache.
 * 
 * The TripleCache defines a filter over a given dataset, whereas the IndexCache
 * partitions the filtered dataset.
 *
 * Partitions can then be accessed by their corresponding key.
 * whenever a partition is accessed, it is first checked whether it exists
 * in memory, and if this is not the case, it is loaded from the underlying
 * store.
 * 
 * 
 * TODO Somehow find a way to have a cache on a "virtual" graph: This means
 * A graph that can be the intersection, union, or difference of multiple
 * graphs.
 * 
 * Some thoughts on this:
 * Maybe it should work something like this:
 * First there is the graph object, this one may have multiple caches and indexes
 * attached to it.
 * 
 * Then a virtual graph can be built based on these graph objects.
 * The virtual graph - like the ordinary graph - allows access of the caches
 * The problem that arises is, that how to determine whether the caches (filters)
 * and indexes are compatible?!
 * 
 * So the whole access stuff should actually be hidden.  
 * 
 * 
 * 
 *  
 * @author raven
 *
 */







class SparqlEndpoint
{
    void insert(Model model, String graphName) {}
    void remove(Model model, String graphName) {}

    Model executeConstruct(String query) { return null; }
}


/**
 * This class is not finished yet.
 * 
 * Some thoughts: It would be cool to have a "construct cache:"
 * 
 * E.g. Construct { ?p :hasName ?name . ?p :hasAddress  ?a . }
 * The graph pattern can be decomposed into clauses:
 * (And(.)
 * 
 * Our cache function is vars(Query) -> Model
 * (e.g. (?p ?a) -> Model) (Hm actually it doesn't matter whether its a model or not)
 * 
 * 
 * Whenever a triple is inserted, that satisfied one of the clauses,  we
 * need to invalidate a corresponding cache entry.
 * 
 * 
 * 
 * @author raven
 *
 */
public class GraphBackedResourceCache
{
	private static final Logger logger = LoggerFactory.getLogger(GraphBackedResourceCache.class);
	
	private String graphName;
	private SparqlEndpoint graphDAO;
	private int batchSize = 1024;
	
	
	// Updates pending for the database
	private ModelDiff pendingUpdates = new ModelDiff();

	private Model cacheData = ModelFactory.createDefaultModel();
	
	private CacheSet<Resource> posCache = new CacheSet<Resource>();
	private CacheSet<Resource> negCache = new CacheSet<Resource>();

	/*
	public static void main(String[] args)
		throws Exception
	{
		Query query = QueryFactory.create("Select * {?s ?p ?o .}");
		
		Dataset dataset = DatasetFactory.create("http://hanne.aksw.org:8892/sparql");
		///ResourceFactory.createResource("http://dbpedia.org"))
		Model model = dataset.getNamedModel("http://dbpedia.org");
		StmtIterator it = model.listStatements(null, RDF.type, (RDFNode)null);
		while(it.hasNext()) {
			System.out.println(it.next());
		}
		
		dataset.close();
		
		//QueryEngineHTTP x = new QueryEngineHTTP();
		//CacheIndexImpl index = new CacheIndexImpl(query, "?s");
		//System.out.println(query.getQueryPattern().varsMentioned().toArray()[0]);
		
		//GraphFactory.
		//ModelFactory.create
	}
	*/
	

    /*
	public static void mainThis(String[] args)
		throws Throwable
	{
		PropertyConfigurator.configure("log4j.properties");
		
		System.out.println("Test");
		
		String graphName = "http://test.org";
		Connection conn = VirtuosoUtils.connect("localhost", "dba", "dba");
		
		ISparulExecutor graphDAO = new VirtuosoJdbcSparulExecutor(conn, graphName);
		
		GraphBackedResourceCache cache = new GraphBackedResourceCache(graphDAO);

		List<Resource> resources = Arrays.asList(new Resource[]{
			ResourceFactory.createResource("http://s.org"),
			ResourceFactory.createResource("http://linkedgeodata.org/triplify/way54888992/nodes")
		});
		
		
		Model m;
		
		m = cache.lookup(resources);
		m = cache.lookup(resources);
		System.out.println(ModelUtil.toString(m));
	}
	*/
	
	public GraphBackedResourceCache(SparqlEndpoint graphDAO)
	{
		this.graphDAO = graphDAO;
	}
	
	public Model lookup(Collection<Resource> resources)
		throws Exception
	{		
		Model result = ModelFactory.createDefaultModel();
		
		Set<Resource> ress = new HashSet<Resource>(resources);

		
		int negCacheHits = 0;
		int posCacheHits = 0;
		
		
		// Check for negative hit
		Iterator<Resource> it = ress.iterator();
		while(it.hasNext()) {
			Resource resource = it.next();
			
			if(negCache.contains(resource)) {
				it.remove();
				++negCacheHits;
				continue;
			}

			if(posCache.contains(resource)) {
				result.add(cacheData.listStatements(resource, null, (RDFNode)null));
				posCache.renew(ress);
				it.remove();
				
				++posCacheHits;
			}
		}
		
		Model lookup = lookupBySubject(graphDAO, ress, graphName, batchSize);
		// Check for which resources we
		
		Set<Resource> subjects = lookup.listSubjects().toSet();
		
		// FIXME If in unlikely case that too many resources are lookup,
		// just purge the cache and fill with as much as possible,
		// rather then updating the cache for each individual resource.
		for(Resource resource : ress) {
			if(subjects.contains(resource)) {
				Resource removed = posCache.addAndGetRemoved(resource);
				if(removed != null) {
					cacheData.remove(removed, null, (RDFNode)null);
				}
				
				cacheData.add(lookup.listStatements(resource, null, (RDFNode)null));
			} else {
				negCache.add(resource);
			}
		}
		
		result.add(lookup);
		
		
		logger.debug("Cache statistics for lookup on " + resources.size() + " resources: posHit/negHit/retrieve = " + posCacheHits + "/" + negCacheHits + "/" + ress.size());
	
		return result;
	}
	
	
	public void insert(Model model)
		throws Exception
	{	
		Model added = ModelFactory.createDefaultModel();
		added.add(model);
		
		// Fetch data for all inserted resources
		Model oldModel = lookup(model.listSubjects().toSet());

		// Remove all triples that already existed
		added.remove(oldModel);
	
		pendingUpdates.add(added);

		for(Resource resource : model.listSubjects().toSet()) {
			if(negCache.contains(resource)) {
				negCache.remove(resource);
				
				Resource removed = posCache.addAndGetRemoved(resource);
				cacheData.remove(removed, null, (RDFNode)null);
			}
			
			if(posCache.contains(resource)) {
				cacheData.add(added.listStatements(resource, null, (RDFNode)null));
			}			
		}
	}

	public void remove(Model model)
		throws Exception
	{	
		Model removed = ModelFactory.createDefaultModel();
		removed.add(model);
		
		// Fetch data for all inserted resources
		Model oldModel = lookup(model.listSubjects().toSet());
	
		// Remove all triples that already existed
		removed.remove(oldModel);
	
		pendingUpdates.remove(removed);
	
		for(Resource resource : model.listSubjects().toSet()) {
			if(negCache.contains(resource)) {
				negCache.remove(resource);
				
				Resource rem = posCache.addAndGetRemoved(resource);
				cacheData.remove(rem, null, (RDFNode)null);
			}
			
			if(posCache.contains(resource)) {
				cacheData.add(removed.listStatements(resource, null, (RDFNode)null));
			}			
		}
	}
	
	
	
	public void applyChanges()
		throws Exception
	{
		graphDAO.remove(pendingUpdates.getRemoved(), graphName);
		graphDAO.insert(pendingUpdates.getAdded(), graphName);
	}
	
	private static Model lookupBySubject(SparqlEndpoint graphDAO, Collection<Resource> subjects, String graphName, int batchSize)
		throws Exception
	{
		Model result = ModelFactory.createDefaultModel();
		
		
		List<List<Resource>> chunks = CollectionUtils.chunk(subjects, batchSize);
		
		for(List<Resource> chunk : chunks) {
			String resources = "<" + Joiner.on(">,<").join(chunk) + ">";
			
			String fromPart = (graphName != null)
				? "From <" + graphName + "> "
				: "";
	
			String query =
				"Construct { ?s ?p ?o . } " + fromPart + "{ ?s ?p ?o . Filter(?s In (" + resources + ")) . }";
	
			Model tmp = graphDAO.executeConstruct(query);
			result.add(tmp);
		}
		
		return result;
	}

	
	
	public static String myToString(Collection<?> collection)
	{
		return "(" + collection.size() + ")" + collection;		
	}

	
	public static Set<List<Object>> toKeys(Collection<Resource> resources) {
		Set<List<Object>> result = new HashSet<List<Object>>();
		
		for(Resource item : resources) {
			result.add(Collections.singletonList((Object)item.asNode()));
		}
		
		return result;
	}
}
	

    /*
	public static void main(String[] args)
		throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		
		List<String> a = Arrays.asList("a");
		List<String> b = new ArrayList<String>(a); 
		List<String> c = new LinkedList<String>(a); 
		List<String> d = Collections.singletonList("a");
		
		System.out.println(a.hashCode());
		System.out.println(b.hashCode());
		System.out.println(c.hashCode());
		System.out.println(d.hashCode());
		
		//if(true)
			//return;
		
		//TripleFilter filter = new TripleFilter("?s = <http://test.org>");
		Random random = new Random(0);
	
		ISparqlExecutor tmpSparqlEndpoint = new SparqlEndpointExecutor("http://localhost:8890/sparql", "http://test.org");
		
		List<QuerySolution> qss = tmpSparqlEndpoint.executeSelect("Select Distinct ?s From <http://Exp3Random.log> {?s ?p ?o. }");
		Set<Resource> subjects = new HashSet<Resource>(); 
		for(QuerySolution qs : qss) {
			subjects.add(qs.getResource("s"));
		}
		
		
		//subjects = RandomUtils.randomSampleSet(subjects, 1000, random);
		
		
		Model tmpModel = tmpSparqlEndpoint.executeConstruct("Construct {?s ?p ?o. } From <http://Exp3Random.log> {?s ?p ?o. }");
		System.out.println("TmpModelSize: " + tmpModel.size());
		
		JenaSparulExecutor sparqlEndpoint = new JenaSparulExecutor(tmpModel);
		
		SparqlEndpointFilteredGraph graph = new SparqlEndpointFilteredGraph(sparqlEndpoint, "http://test.org");
		
		TripleCacheIndexImpl.create(graph, 100000, 100000, 100000, 0);
		
		
		Node n = Node.createURI("http://nke/Exp3Random/Actors_from_Tennessee/fold/1/phase/1/5");
		
		
		DeltaGraph deltaGraph = new DeltaGraph(graph);

		Set<Triple> triples = new HashSet<Triple>();
		
		Triple myTriple = new Triple(n, Node.createURI("http://p"), Node.createURI("http://o")); 
		triples.add(myTriple);
		
		deltaGraph.add(triples);
		deltaGraph.remove(triples);
		
		
		Set<Triple> qr = deltaGraph.bulkFind(
				Collections.singleton(Collections.singletonList((Object)n)), 
				new int[]{0});
		
		System.out.println(myToString(qr));

		System.out.println(deltaGraph.getBaseGraph());

				///*
		for(int i = 0; i < 100000; ++i) {
			Set<Resource> resources = RandomUtils.randomSampleSet(subjects, 1000, random);
			
			Set<List<Object>> keys = toKeys(resources);
			
			logger.trace("Finding " + keys.size() + " keys");
			Set<Triple> cache = deltaGraph.bulkFind(keys, new int[]{0});
			
			
			/*
			Set<Triple> store = deltaGraph.getBaseGraph().uncachedBulkFind(keys, new int[]{0});
			
			if(!cache.equals(store))
				throw new RuntimeException("DAMMIT");
			* /
			
			//System.out.println(i + ": "+ deltaGraph.getBaseGraph());
			
			if(i % 1000 == 0) {
				System.out.println(i + ": "+ deltaGraph.getBaseGraph());

				//System.out.println("HERE");
			}
		}//*/
		
		
		
		
		//System.out.println("Triples: #" + triples.size() + ": " + triples);
		
		//triples = graph.bulkFind(keys, new int[]{0});
		
		//System.out.println("Triples: #" + triples.size() + ": " + triples);
		

		//graph.bulkFind(keys, indexColumns)
		
		
		
		
		//new TripleCacheIndexImpl(graph, 0);
		
		
		//graph.getCacheProvider().getIndexes().add();
		
		
		
		
		/*

 http://nke/Exp3Random/Actors_from_Tennessee/fold/1/phase/1/5
http://nke/Exp3Random/Actors_from_Tennessee/fold/1/phase/1/11/14
http://nke/Exp3Random/Actors_from_Tennessee/fold/1/phase/1/11/12
http://nke/Exp3Random/Actors_from_Tennessee/fold/1/phase/1/3
 
 
 
		 * /
		String[] resourceStrs = {
				"http://nke/Exp3Random/Actors_from_Tennessee/fold/1/phase/1/5",
				"http://nke/Exp3Random/Actors_from_Tennessee/fold/1/phase/1/11/14",
				"http://nke/Exp3Random/Actors_from_Tennessee/fold/1/phase/1/3" };
		
		
		List<List<Object>> resources = new ArrayList<List<Object>>();
		for(String resourceStr : resourceStrs) {
			Resource resource = ResourceFactory.createResource(resourceStr); 
			resources.add(Collections.singletonList((Object)resource));
		}
		
		List<QuerySolution> rs = sparqlEndpoint.executeSelect("Select Distinct ?s From <http://Exp3Random.log> {?s ?p ?o .} Limit 20");
		for(QuerySolution qs : rs) {
			System.out.println(qs.get("s"));
		}
		
		
		
		
		//System.exit(1);
		
		//FilteredGraph
		
		//sparqlEndpoint.
		//TripleCache cache = new TripleCache(sparqlEndpoint, Collections.singleton("http://test.org"), "?p = <" + RDF.type + ">");
		
		//TripleCacheIndexImpl sIndex = new TripleCacheIndexImpl(cache, 0);
	
		
		//sIndex.get(resources);
		
		//Model model = sIndex.getModel(resources);
		
		
		//model.write(System.out, "N3");
		
	}
	
}
*/
