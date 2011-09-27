package org.aksw.commons.sparql;

import static org.junit.Assert.*;

import org.junit.Test;

public class SPARQLEndpointsTest
{

	@Test
	public void testGetMostCommonResourcePrefix()
	{
		assertTrue(SPARQLEndpoints.getMostCommonResourcePrefix(SPARQLEndpoints.DBPEDIA).equals("http://dbpedia.org/resource/"));
	}

}
