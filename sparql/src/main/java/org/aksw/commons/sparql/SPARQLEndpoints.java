package org.aksw.commons.sparql;

import java.util.HashMap;
import java.util.Map;

public class SPARQLEndpoints
{
	public static final String DBPEDIA 			= "http://dbpedia.org/sparql";
	public static final String DBPEDIA_LIVE		= "http://live.dbpedia.org/sparql";
	public static final String LINKEDGEODATA 		= "http://linkedgeodata.org/sparql";
	public static final String LINKEDGEODATA_LIVE	= "http://live.linkedgeodata.org/sparql";
	public static final String LINKEDMDB			= "http://data.linkedmdb.org/sparql";

	private static Map<String,String> endpointToPrefixMap = null;	
	private static void initMap()
	{
		endpointToPrefixMap = new HashMap<String,String>();
		for(String[] row: endpointToPrefix) {endpointToPrefixMap.put(row[0],row[1]);}
		}

	/** @return the most common resource prefix for a sparql endpoint, e.g.
	 * "http://dbpedia/sparql" -> "http://dbpedia/resource".
	 * This is useful to guess from which endpoint an instance originally came from.
	 * */
	public static String prefix(String endpoint)
	{
		if(endpointToPrefixMap==null) {initMap();}
		return endpointToPrefixMap.get(endpoint);
	}

	private final static String[][] endpointToPrefix =
		{
		{DBPEDIA,"http://dbpedia.org/resource/"},
		{LINKEDGEODATA,"http://linkedgeodata.org/triplify/"},
		{LINKEDMDB,"http://data.linkedmdb.org/resource/film/"},
		};
}