package org.aksw.commons.collections;

import static org.junit.Assert.assertTrue;
import static org.aksw.commons.collections.Sets.*;
import static org.aksw.commons.collections.CollectionUtils.*;
import org.junit.Test;

public class SetsTest
{
	@Test
	public void testAll()
	{
		assertTrue(asSet(new String[] {"b","d","e"}).equals(
				complement(
				difference(
				intersection(
				union(asSet(new String[] {"a","b","e"}),asSet(new String[] {"c"})),
				asSet(new String[] {"a","c","d","e"})),
				asSet(new String[] {"e"})),
				asSet(new String[] {"a","b","c","d","e"}))));
	}
}