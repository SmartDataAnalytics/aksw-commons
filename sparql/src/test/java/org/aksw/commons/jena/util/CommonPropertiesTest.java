package org.aksw.commons.jena.util;

import java.util.Map;

import org.aksw.commons.sparql.SPARQLEndpoints;
import org.junit.Test;

public class CommonPropertiesTest
{

	@Test
	public void testGetCommonProperties()
	{	
		String where = "?s a dbpedia-owl:Settlement";
		Map<String,Integer> properties = CommonProperties.getCommonProperties(SPARQLEndpoints.DBPEDIA, where, 0.8,10, 1000);
		System.out.println();
	}
}