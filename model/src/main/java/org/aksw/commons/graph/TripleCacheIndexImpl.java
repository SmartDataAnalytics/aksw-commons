package org.aksw.commons.graph;


import com.google.common.base.Joiner;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import org.apache.commons.collections15.map.LRUMap;

import java.util.*;



/**
 * FIXME The index could be used with either LRUMaps or Maps for keeping
 * track of the data. So these indexes could be used both as cache or as
 * full indexes.
 *  
 * 
 * 
 * @author raven
 *
 */
public class TripleCacheIndexImpl
		implements ITripleCacheIndex
{
	private IGraph								graph;

	/**
	 * Enables/Disables tracking of incomplete partitions. Incomplete partitions
	 * can be used for answering queries for existence, however, they cannot be
	 * iterated without fetching all data (completing them)first. Therefore, if
	 * existence checks (such is there a link between :s ?p :o) are not
	 * required, disable this tracking.
	 * 
	 * 
	 */
	private boolean								trackIncompletePartitions	= true;

	/**
	 * Maps a pattern (e.g. s, null, null)
	 * 
	 */
	private Map<List<Object>, Set<List<Object>>> full;
	private Map<List<Object>, Set<List<Object>>> partial;// = new LRUMap<List<Object>,  Set<List<Object>>>();
	
	
	//private LRUMap<List<Object>, IndexTable>	keyToValues					= new LRUMap<List<Object>, IndexTable>();
	private Set<List<Object>>				noDataCache;//					= new CacheSet<List<Object>>();

	private int[]								indexColumns;
	private int[]								valueColumns;

	public int[] getIndexColumns()
	{
		return indexColumns;
	}

	
	/*
	public static Map<List<Object>, Set<List<Object>>> createMap(Integer maxSize)
	{		
		return (maxSize == null)
			? new HashMap<List<Object>, Set<List<Object>>>()
			: new LRUMap<List<Object>, Set<List<Object>>>(maxSize);
	}
	*/
	
	
	
	public static TripleCacheIndexImpl create(IGraph graph,
			Integer fullMaxSize,
			Integer partialMaxSize,
			Integer emptyMaxSize,
			int...indexColumns
	) throws Exception {
		
		
		Map<List<Object>, Set<List<Object>>> full = TripleIndexUtils.createMap(fullMaxSize);
		Map<List<Object>, Set<List<Object>>> partial = TripleIndexUtils.createMap(partialMaxSize);
		Set<List<Object>> set = TripleIndexUtils.createSet(emptyMaxSize);
		
		TripleCacheIndexImpl index =
			new TripleCacheIndexImpl(
					graph,
					indexColumns,
					full,
					partial,
					set);

		graph.getCacheProvider().getIndexes().add(index);
		
		return index;
	}
	
		
	/**
	 * Index columns: 0: subject 1: predicate 2: object
	 * 
	 * @param filter
	 * @param indexColumns
	 * @throws Exception
	 */
	private TripleCacheIndexImpl(
			IGraph graph,
			int[] indexColumns,
			Map<List<Object>, Set<List<Object>>> full,
			Map<List<Object>, Set<List<Object>>> partial,
			Set<List<Object>>				noDataCache)
			throws Exception
	{
		this.graph = graph;
		this.indexColumns = indexColumns;

		this.valueColumns = TripleIndexUtils.getValueColumns(indexColumns);
		// Delta deltaGraph = new Delta(baseGraph);
		
		this.full = full;
		this.partial = partial;
		this.noDataCache = noDataCache;

		//new LRUMap<List<Object>,  Set<List<Object>>>();
	}

	public static RDFNode getItemAt(Statement stmt, int index)
	{
		switch (index) {
		case 0:
			return stmt.getSubject();
		case 1:
			return stmt.getPredicate();
		case 2:
			return stmt.getObject();
		default:
			throw new IndexOutOfBoundsException();
		}
	}

	/*
	 * public static void (Statement stmt, int index, Object value) {
	 * switch(index) { case 0: { stmt. case 1: return stmt.getPredicate(); case
	 * 2: return stmt.getObject(); default: throw new
	 * IndexOutOfBoundsException(); } }
	 */

	public List<Object> extractKey(Statement stmt)
	{
		Object[] keyTmp = new Object[indexColumns.length];
		for (int i = 0; i < indexColumns.length; ++i) {
			keyTmp[i] = getItemAt(stmt, indexColumns[i]);
		}
		return Arrays.asList(keyTmp);
	}


	public static <T> IndexTable getOrCreate(Map<List<T>, IndexTable> map,
			List<T> key)
	{
		IndexTable result = map.get(key);
		if (result == null) {
			result = new IndexTable();
			map.put(key, result);
		}
		return result;
	}

	public static <T> IndexTable getOrCreate(LRUMap<List<? super T>, IndexTable> map,
			List<T> key)
	{
		IndexTable result = map.get(key);
		if (result == null) {
			result = new IndexTable();
			map.put(key, result);
		}
		return result;
	}

	/*
	public Model getModel(List<List<Object>> keys) throws Exception
	{
		Model result = ModelFactory.createDefaultModel();

		Map<List<Object>, IndexTable> map = get(keys);

		for (Map.Entry<List<Object>, IndexTable> entry : map.entrySet()) {

			Object[] objects = new Object[3];
			for (int i = 0; i < indexColumns.length; ++i) {
				objects[indexColumns[i]] = entry.getKey().get(i);
			}

			for (List<Object> row : entry.getValue().getRows()) {
				for (int i = 0; i < valueColumns.length; ++i) {
					objects[valueColumns[i]] = row.get(i);
				}

				result.add((Resource) objects[0], (Property) objects[1],
						(RDFNode) objects[2]);
			}

		}

		return result;
	}
	*/

	
	public static <T> void fill(T[] array, Iterable<T> items, int[] map)
	{
		Iterator<T> it = items.iterator();
		
		for(int i = 0; i < map.length; ++i) {
			T item = it.next();
			array[map[i]] = item;
		}
	}
	
	
	public static Triple toTriple(Object[] array) {
		return new Triple((Node)array[0], (Node)array[1], (Node)array[2]);
	}

	
	void validate(List<Object> key)
	{
		boolean match = false;
		if(key.get(0).toString().contains("Literary_collaborations/fold/3/phase/1/144")) {
			match = true;
			System.out.println("HERE");
		}
			
		
		Set<List<Object>> table = full.get(key);
		if(table == null) {
			
			for(Map.Entry<List<Object>, Set<List<Object>>> entry : full.entrySet()) {
				
				for(int i = 0; i < entry.getKey().size(); ++i) {
					
					if(match && entry.getKey().get(i).toString().contains("Literary_collaborations/fold/3/phase/1/144")) {

						System.out.println("DAMMIT");
						System.out.println(key);
						System.out.println(entry.getKey());
						
					}
					/*
					if(!key.get(i).toString().equals(entry.getKey().get(i).toString())) {
						match = false;
						break;
					}*/
				}

				/*
				if(match == true) {
					System.out.println("DAMMIT");
					System.out.println(key);
					System.out.println(entry.getKey());
				}*/
			}
			
		}
		
	}
	
	/**
	 * lookup: returns a possible empty set of triples
	 * or null - if no 
	 * 
	 * Does not track cache misses.
	 * 
	 * @param key
	 * @return
	 */
	@Override
	public Collection<Triple> lookup(List<Object> key) {
		
		Set<List<Object>> table = full.get(key);
		if (table == null) {
			// Potential cache miss
			if (noDataCache.contains(key)) {
				return Collections.emptySet();
			}
		} else {
			// Cache hit
			Collection<Triple> result = new HashSet<Triple>();
			for(List<Object> value : table) {
				Object[] array = new Object[3];
				
				fill(array, key, indexColumns);
				fill(array, value, valueColumns);
				
				Triple triple = toTriple(array);
				result.add(triple);
			}
			return result;
		}

		return null;
	}
	
	/*
	public Map<List<Object>, IndexTable> get(List<List<Object>> keys)
			throws Exception
	{

		Map<List<?>, IndexTable> result = new HashMap<List<?>, IndexTable>();

		List<List<?>> unresolveds = new ArrayList<List<?>>();
		for (List<?> key : keys) {

			IndexTable table = keyToValues.get(key);
			if (table == null || !table.isComplete()) {
				// Potential cache miss
				if (noDataCache.contains(key)) {
					continue;
				}

				unresolveds.add(key);
			} else {
				// Cache hit

				IndexTable resultTable = getOrCreate(result, key);
				resultTable.getRows().addAll(table.getRows());
			}
		}

		// Ask the underlying cache - none of the index business
		Model model = cache.construct(keys, indexColumns);

		index(model, true);

		// Perform a lookup for all unresolved resources
		// listStatements(map(key);
		return null;
	}

	private void index(Model model, boolean isComplete)
	{
		for (Statement stmt : new ModelSetView(model)) {
			List<Object> key = new ArrayList<Object>();

			List<Object> value = new ArrayList<Object>();

			for (int index : indexColumns) {
				key.add(getItemAt(stmt, index));
			}

			for (int index : valueColumns) {
				value.add(getItemAt(stmt, index));
			}

			// keyToValues.get(key);
			// IndexTable table = getOrCreate(keyToValues, key);

			IndexTable table = keyToValues.get(key);
			if (table == null) {
				table = new IndexTable(isComplete);
				keyToValues.put(key, table);
			}

			table.getRows().add(value);
		}
	}
	*/

	@Override
	public IGraph getGraph()
	{
		return graph;
	}

	@Override
	public IndexCompatibilityLevel getCompatibilityLevel(Triple pattern)
	{
		int count = 0;
		for (int indexColumn : indexColumns) {
			Node node = TripleUtils.get(pattern, indexColumn);

			count += (node != null) ? 1 : 0;
		}

		if (count == 0) {
			return IndexCompatibilityLevel.NONE;
		} else if (count == indexColumns.length) {
			return IndexCompatibilityLevel.FULL;
		} else {
			return IndexCompatibilityLevel.PARTIAL;
		}
	}

	/*
	 * // pattern (s, null, o) -> 101 private static int patternToMask(Triple
	 * triple) { int result = 0;
	 * 
	 * for(int i = 0; i < 3; ++i) { result |= ((TripleUtils.get(triple, 0) !=
	 * null) ? 1 : 0) << i; }
	 * 
	 * return result; }
	 * 
	 * // index (s) -> 001 // index (so) -> 101 private static int
	 * columnsToMask(int[] columnIds) { int result = 0;
	 * 
	 * for(int id : columnIds) { result |= 1 << id; }
	 * 
	 * return result; }
	 */
	private List<Object> tripleToKey(Triple triple)
	{
		return TripleIndexUtils.tripleToList(triple, indexColumns);
	}

	private List<Object> tripleToValue(Triple triple)
	{
		return TripleIndexUtils.tripleToList(triple, valueColumns);
	}

	/*
	private List<Object> tripleToList(Triple triple, int[] indexes)
	{
		Object[] array = new Object[indexes.length];
		for (int i = 0; i < array.length; ++i) {
			array[i] = TripleUtils.get(triple, indexes[i]);
		}

		List<Object> result = Arrays.asList(array);
		return result;
	}
*/

	/**
	 * Note: When indexing too many triples, the LRU map might be full.
	 * We therefore 
	 * 
	 * 
	 */
	@Override
	public void index(Collection<Triple> triples)
	{
		/*
		Map<List<Object>, IndexTable> tmp = new HashMap<List<Object>, IndexTable>();
		
		for (Triple triple : triples) {
			List<Object> key = tripleToKey(triple);
			List<Object> value = tripleToValue(triple);

			IndexTable table = getOrCreate(tmp, key);

			// FIXME If the table is complete, can an incomplete insertion
			// make the table incomplete?! I think no.
			// Incomplete only means that there might be triples which have
			// not yet been by the cache. However, if the table is complete,
			// the cache already knows all triples. The triples that are being
			// inserted are additional triples for indexing.
			// NOTE Incomplete does not mean that there is a lack of information
			// e.g. triples not sent to the cache, but merely that not all
			// triples for a partition have been fetched yet.
			table.setComplete(true);

			table.getRows().add(value);
		}*/
		Map<List<Object>, Set<List<Object>>> tmp = TripleIndexUtils.index2(triples, indexColumns);
		
		for(Map.Entry<List<Object>, Set<List<Object>>> entry : tmp.entrySet()) {
			partial.remove(entry.getKey());
			noDataCache.remove(entry.getKey());
			
			full.put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void removeSeen(Collection<Triple> triples)
	{
		for (Triple triple : triples) {
			List<Object> key = tripleToKey(triple);
			List<Object> value = tripleToValue(triple);

			
			Map<List<Object>, Set<List<Object>>> source = full;
			
			Set<List<Object>> table = source.get(key);
			if (table == null) {
				source = partial;

				table = partial.get(key);
				
				if(table == null) {
					continue;
				}
			}

			table.remove(value);
			if(table.isEmpty()) {
				source.remove(key);
				
				if(source == full) {
					noDataCache.add(key);
				}
			}
		}
	}

	@Override
	public int[] getKeyColumns()
	{
		return indexColumns;
	}

	@Override
	public int[] getValueColumns()
	{
		return valueColumns;
	}

	@Override
	public IndexCompatibilityLevel getCompatibilityLevel(int[] columnIds)
	{
		Set<Integer> cs = new HashSet<Integer>();
		for(int id : columnIds)
			cs.add(id);
		
		int count = 0;
		for (int indexColumn : indexColumns) {
			count += cs.contains(indexColumn) ? 1 : 0;
		}

		if (count == 0) {
			return IndexCompatibilityLevel.NONE;
		} else if (count == indexColumns.length) {
			return IndexCompatibilityLevel.FULL;
		} else {
			return IndexCompatibilityLevel.PARTIAL;
		}

	}

/*
	@Override
	public void addSeen(Collection<Triple> triples)
	{
		for (Triple triple : triples) {
			List<Object> key = tripleToKey(triple);
			List<Object> value = tripleToValue(triple);

			Set<List<Object>> table = full.get(key);
			if (table == null) {
				table = partial.get(key);
				
				if(table == null) {
					continue;
				}
			}

			// TODO We assume that the rows in the table are unique
			// but maybe this is the best way to do it in the first place
			table.add(value);
			noDataCache.remove(key);
		}
	}
*/
	
	@Override
	public void addSeen(Collection<Triple> triples)
	{
		for (Triple triple : triples) {
			List<Object> key = tripleToKey(triple);
			List<Object> value = tripleToValue(triple);

			Set<List<Object>> table = full.get(key);
			if (table == null) {

				if(noDataCache.contains(key)) {
					table = new HashSet<List<Object>>();
					full.put(key, table);
					noDataCache.remove(key);
				}
				else {
					table = partial.get(key);
				
					// FIXME Shouldn't we create a partial table here?
					if(table == null) {
						table = new HashSet<List<Object>>();
						partial.put(key, table);
					}
				}
			}

			// TODO We assume that the rows in the table are unique
			// but maybe this is the best way to do it in the first place
			table.add(value);
		}
	}
	
	
	@Override
	public void clear()
	{
		full.clear();
		partial.clear();
		noDataCache.clear();
	}

	
	@Override
	public String toString()
	{
		return
			"Full/Partial/None: " + Joiner.on("/").join(full.size(), partial.size(), noDataCache.size());
	}


	@Override
	public void registerMisses(Set<List<Object>> keys)
	{
		noDataCache.addAll(keys);
	}


	@Override
	public CacheState getState()
	{
		return new CacheState(full.size(), partial.size(), noDataCache.size());
	}
	
	/**
	 * Using ask it is possible to check for the existence of entries In this
	 * case incomplete cache entries do not matter
	 * 
	 * @param keys
	 * @return public Map<List<Object>, Boolean> ask(Collection<List<Object>>
	 *         keys) { }
	 */
}
