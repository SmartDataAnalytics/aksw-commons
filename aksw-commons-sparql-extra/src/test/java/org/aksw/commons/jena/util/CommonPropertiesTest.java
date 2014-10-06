package org.aksw.commons.jena.util;

import org.aksw.commons.sparql.SPARQLEndpoints;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class CommonPropertiesTest
{
	@Test
	public void testGetCommonProperties()
	{	
		String where = "?s a dbpedia-owl:Settlement";
		Map<String,Integer> properties = CommonProperties.getCommonProperties(SPARQLEndpoints.DBPEDIA_LIVE, where, 0.8,20, 50);
		//System.out.println(properties);
		Assert.assertTrue(properties.get("http://www.w3.org/1999/02/22-rdf-syntax-ns#type") == 50);
	}
}