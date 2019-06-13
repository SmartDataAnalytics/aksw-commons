package org.aksw.commons.collections.generator;

import org.junit.Test;

import junit.framework.Assert;

public class TestGeneratorLendingImpl {

	@Test
	public void testGeneratorLendingImpl() {
		GeneratorLending<Integer> gen = GeneratorLendingImpl.createInt(1);
		Assert.assertEquals(1, (int)gen.next());

		gen.giveBack(1);
		
		Assert.assertEquals(1, (int)gen.next());		
		Assert.assertEquals(2, (int)gen.next());		
		Assert.assertEquals(3, (int)gen.next());		

		gen.giveBack(3);
		gen.giveBack(1);

		Assert.assertEquals(1, (int)gen.next());		
		Assert.assertEquals(3, (int)gen.next());		
		Assert.assertEquals(4, (int)gen.next());

		gen.giveBack(2);
		Assert.assertEquals(2, (int)gen.next());
		Assert.assertEquals(5, (int)gen.next());	
	}
}
