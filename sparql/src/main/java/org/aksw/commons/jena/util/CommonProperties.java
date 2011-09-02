package org.aksw.commons.jena.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/** @author Konrad Höffner */
public class CommonProperties
{
	// TODO: finish method / change from cache to direct endpoint querying
//	/**
//	 * @param threshold a value between 0 and 1, specifying what fraction of the instances must have this property 
//	 * for it to be counted as common property. Set to 0 or null if you want no restriction on this. 
//	 * @param limit a non-negative integer value, specifying the maximum amount of properties to return.
//	 * If there are more than {@link limit} after the exclusion with {@threshold}, the most common properties of those are returned. 
//	 * @param sampleSize the number of instances whose triples are examined. Set to null to look at all triples (may take a long time).
//	 * On the other hand, using a sample instead of all data may give a wrong result even for a big sample size because the sample is not random
//	 * but the selection depends on the SPARQL server (uses Virtuoso SPARQL for subqueries).
//	 * @return the most common properties sorted by occurrence in descending order.
//	 * Each property p is counted at most once for each instance s, even if there are multiple triples (s,p,o).
//	 * Example: getCommonProperties(0.5) will only return properties which are used by at least half of the uris in the cache.
//	 */
//	public String[] getCommonProperties(String endpoint, String restriction, Double threshold, Integer limit, Integer sampleSize)
//	{		
//		if(threshold!=null&&(threshold<0||threshold>1))		throw new IllegalArgumentException("parameter relativeThreshold must lie between 0 and 1 inclusively.");
//		if(limit!=null&&(limit<0)) 							throw new IllegalArgumentException("parameter limit may not be negative.");
//		if(sampleSize!=null&&(sampleSize<0)) 				throw new IllegalArgumentException("parameter limit may not be negative.");
//		
//		String query = ;
//		
//		final HashMap<String,Integer> propertyOccurrences = new HashMap<String,Integer>();
//		for(String uri: this.instanceMap.keySet())
//		{
//			Instance instance = instanceMap.get(uri);
//			for(String property: instance.properties.keySet())
//			{
//				if(!propertyOccurrences.containsKey(property))
//				{
//					propertyOccurrences.put(property, 1);
//				} else
//				{
//					propertyOccurrences.put(property,propertyOccurrences.get(property)+1);
//				}
//			}
//		}
//
//		List<String> allProperties = new LinkedList<String>(propertyOccurrences.keySet());		
//		// sort by occurrence in descending order
//		
//		Collections.sort(allProperties,
//				new Comparator<String>()
//				{
//					@Override
//					public int compare(String p1, String p2)
//					{
//						int c = -(new Integer(propertyOccurrences.get(p1)).compareTo(propertyOccurrences.get(p2))); 
//						// natural order is ascending but we want descending order, thats why the minus is there
//						if(c!=0) return c;
//						return p1.compareTo(p2);
//					}
//				}
//		);
//		
//		//for(String property:allProperties) {System.out.println("\\nolinkurl{"+PrefixHelper.abbreviate(URLDecoder.decode(property))+"}			&"+propertyOccurrences.get(property)+"\\\\");}
//		
//		if(threshold==null&&limit==null){return propertyOccurrences.keySet().toArray(new String[0]);}
//
//		int absoluteThreshold = (int) (threshold * instanceMap.size());  				
//
//		List<String> properties = new LinkedList<String>();
//		
//		for(String property: allProperties)
//		{
//			if(properties.size()>=limit) break;
//			if(propertyOccurrences.get(property)>=absoluteThreshold) {properties.add(property);}			
//		}
//		
//		
//		return properties.toArray(new String[0]);		
//	}
}
