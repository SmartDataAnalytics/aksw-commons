package org.aksw.commons.strings;

import org.aksw.commons.util.string.StringUtils;
import org.junit.Test;

import junit.framework.Assert;

public class TestFindCharPos {
	@Test
	public void testFindCharPos() {
		// charpos    0123 4 567 89 0 12345
		// col idx    1234    12 12    1234
		String str = "This\r\nis\na\n\rtest";

		Assert.assertEquals(4, StringUtils.findCharPos(str, 1, 5));

		Assert.assertEquals(0, StringUtils.findCharPos(str, 1, 1));
		Assert.assertEquals(15, StringUtils.findCharPos(str, 4, 4));

		Assert.assertEquals(-1, StringUtils.findCharPos(str, 0, 0));
		Assert.assertEquals(-1, StringUtils.findCharPos(str, 1, 0));

		Assert.assertEquals(-1, StringUtils.findCharPos(str, 1, 666));	
		//Assert.assertEquals(5, StringUtils.findCharPos(str, 2, 0));	
		Assert.assertEquals(-1, StringUtils.findCharPos(str, 2, 0));	
	}
}
