package org.aksw.commons.jena.util;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.aksw.commons.sparql.SPARQLEndpoints;
import org.junit.Test;

public class CachedCommonPropertiesTest
{
	@Test
	public void testGetCommonProperties() throws IOException
	{	
		File cacheFile = new File("tmp");
		cacheFile.delete();
		for(int i=0;i<3;i++)
		{
			CachedCommonProperties ccp = new CachedCommonProperties(cacheFile, SPARQLEndpoints.DBPEDIA_LIVE,0.8,20, 50);
			String where = "?s a dbpedia-owl:Settlement";
			Map<String,Integer> properties = ccp.getCommonProperties(where);
			ccp.save();
			assertTrue(properties.get("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")==50);
		}
	}
}