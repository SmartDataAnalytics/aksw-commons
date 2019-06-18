package org.aksw.commons.collections.cluster;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

import junit.framework.Assert;

public class TestIndirectEquivMap {

	@Test
	public void testEquivMap() {
		IndirectEquiMap<String, Integer> map = new IndirectEquiMap<>();
		
		map.put("a", 1);
		map.put("b", 1);

		map.stateEqual("a", "b");

		map.add("c");
		map.put("c", 1);
		//map.stateEqual("a", "c");

		for(Entry<Integer, Collection<String>> e : map.getEquivalences().asMap().entrySet()) {
			map.setValue(e.getKey(), 2);
		}
		
		Map<Set<String>, Integer> actual = map.dump();

		Map<Set<String>, Integer> expected = new HashMap<>();
		expected.put(Sets.newHashSet("a", "b"), 2);
		expected.put(Sets.newHashSet("c"), 2);
		
		Assert.assertEquals(expected, actual);
	}
}
