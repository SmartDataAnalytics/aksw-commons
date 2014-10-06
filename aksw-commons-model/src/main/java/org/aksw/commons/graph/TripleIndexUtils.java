package org.aksw.commons.graph;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Resource;
import org.aksw.commons.collections.CacheSet;
import org.apache.commons.collections15.map.LRUMap;

import java.util.*;

public class TripleIndexUtils
{
	public static List<Object> getMatchingKey(Set<List<Object>> keys, Triple triple, int[] keyColumns)
	{
		List<Object> key = tripleToList(triple, keyColumns);
		return keys.contains(key) ? key : null;
	}

	
	public static <T> Set<List<Object>> getOrCreate2(Map<List<T>, Set<List<Object>>> map,
			List<T> key)
	{
		Set<List<Object>> result = map.get(key);
		if (result == null) {
			result = new HashSet<List<Object>>();
			map.put(key, result);
		}
		return result;
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

	public static List<Object> tripleToList(Triple triple, int[] indexes)
	{
		Object[] array = new Object[indexes.length];
		for (int i = 0; i < array.length; ++i) {
			array[i] = TripleUtils.get(triple, indexes[i]);
		}

		if(array.length == 1) {
			return Collections.singletonList(array[0]);
		} else {
			List<Object> result = new ArrayList<Object>();
			for(Object item : array) {
				result.add(item);
			}
			
			return result;
		}
	}
	
	
	public static Map<List<Object>, Set<List<Object>>> index2(Collection<Triple> triples, int[] keyColumns)
	{
		return index2(triples, keyColumns, getValueColumns(keyColumns));
	}
	
	public static Map<List<Object>, Set<List<Object>>> index2(Collection<Triple> triples, int[] keyColumns, int[] valueColumns)
	{
		Map<List<Object>, Set<List<Object>>> tmp = new HashMap<List<Object>, Set<List<Object>>>();
		
		for (Triple triple : triples) {
			List<Object> key = tripleToList(triple, keyColumns);
			List<Object> value = tripleToList(triple, valueColumns);

			getOrCreate2(tmp, key).add(value);
		}
		
		return tmp;
	}
	
	
	public static Map<List<Object>, IndexTable> index(Collection<Triple> triples, int[] keyColumns)
	{
		return index(triples, keyColumns, getValueColumns(keyColumns));
	}
	
	public static Map<List<Object>, IndexTable> index(Collection<Triple> triples, int[] keyColumns, int[] valueColumns)
	{
		Map<List<Object>, IndexTable> tmp = new HashMap<List<Object>, IndexTable>();
		
		for (Triple triple : triples) {
			List<Object> key = tripleToList(triple, keyColumns);
			List<Object> value = tripleToList(triple, valueColumns);

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
		}
		
		return tmp;
	}
	
	public static int[] getValueColumns(int[] keyColumns) {
		int[] valueColumns = new int[3 - keyColumns.length];
		
		Set<Integer> cs = new HashSet<Integer>();
		for(int index : keyColumns)
			cs.add(index);
		
		Arrays.asList(0, 1, 2).remove(cs);
		
		int j = 0;
		for (int i = 0; i < 3; ++i) {
			if (cs.contains(i)) {
				continue;
			}

			valueColumns[j++] = i;
		}
		
		return valueColumns;
	}


	public static Set<List<Object>> toKeys(List<Resource> resources)
	{
		Set<List<Object>> result = new HashSet<List<Object>>();

		for(Resource resource : resources) {
			result.add(Collections.singletonList((Object)resource.asNode()));
		}

		return result;
	}
	
	public static <K, V> Map<K, V> createMap(Integer maxSize)
	{		
		if(maxSize == 0) {
			maxSize = 1;
		}
		
		return (maxSize == null)
			? new HashMap<K, V>()
			: new LRUMap<K, V>(maxSize);
	}
	
	public static <T> Set<T> createSet(Integer maxSize) {
		return (maxSize == null)
			? new HashSet<T>()
			: new CacheSet<T>(maxSize, true);
	}
}
