package org.aksw.commons.collections.random;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/** @author Konrad Höffner */
public class RandomUtilsTest
{
	@Test
	public void testRandomSample()
	{
		String[] s = {"Paul","Ringo","Leonardo","Eduardo"};
		Set<String> sSet = new HashSet<String>(Arrays.asList(s));
		String[] t = RandomUtils.randomSample(s, s.length);
		Set<String> tSet = new HashSet<String>(Arrays.asList(t));
		assertTrue(sSet.equals(tSet));		
	}

}