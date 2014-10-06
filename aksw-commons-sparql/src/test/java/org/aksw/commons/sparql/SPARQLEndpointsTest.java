package org.aksw.commons.sparql;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SPARQLEndpointsTest
{

	@Test
	public void testGetMostCommonResourcePrefix()
	{
		assertTrue(SPARQLEndpoints.prefix(SPARQLEndpoints.DBPEDIA).equals("http://dbpedia.org/resource/"));
	}

}
