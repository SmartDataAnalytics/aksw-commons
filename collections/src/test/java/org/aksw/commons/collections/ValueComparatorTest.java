package org.aksw.commons.collections;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertTrue;

public class ValueComparatorTest {

	@Test
	public void testValueComparator()
	{
		Map<String, Integer> gehalt = new HashMap<String,Integer>();
		gehalt.put("Peter",2000);
		gehalt.put("Schmeter",4000);
		gehalt.put("Beter",3000);
		gehalt.put("Seter",1000);
		List<String> personen = new ArrayList<String>(gehalt.keySet());
		List<String> shouldBe = Arrays.asList(new String[] {"Seter", "Peter", "Beter", "Schmeter"});
		Collections.sort(personen, new ValueComparator<String,Integer>(gehalt));
		assertTrue(personen.equals(shouldBe));
	}
}