package org.aksw.commons.collections.reversible;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

import com.google.common.collect.Multimaps;

import junit.framework.Assert;

public class ReversibleMapTest {
	@Test
	public void testReversibleMap() {
		ReversibleMap<String, Integer> map = new ReversibleMapImpl<>();
		ReversibleSetMultimap<Integer, String> rmap = map.reverse();

		map.put("a", 1);
		map.put("b", 2);
		map.put("c", 1);

		rmap.put(2, "d");

		Assert.assertEquals(new HashSet<>(Arrays.asList("a", "c")), rmap.get(1));
		Assert.assertEquals(new HashSet<>(Arrays.asList("b", "d")), rmap.get(2));

		rmap.removeAll(1);
		rmap.remove(2, "d");


		Assert.assertEquals(Collections.singletonMap("b", 2), map);
		Assert.assertEquals(rmap, Multimaps.forMap(Collections.singletonMap(2, "b")));


		rmap.clear();

		Assert.assertEquals(true, map.isEmpty());
		Assert.assertEquals(true, rmap.isEmpty());

	}
}
