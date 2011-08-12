package org.aksw.commons.collections;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class CollectionUtilsTest {

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
		Collections.sort(personen, new CollectionUtils.ValueComparator<String,Integer>(gehalt));
		assertTrue(personen.equals(shouldBe));
	}
}