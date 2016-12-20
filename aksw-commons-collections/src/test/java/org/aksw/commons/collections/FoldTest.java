package org.aksw.commons.collections;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FoldTest {

	@Test
	public void testFolds() {
		List<String> pos = Arrays.asList("a", "b", "c", "d", "e");
		List<String> neg = Arrays.asList("x", "y", "z");

		List<Fold<String>> folds = Fold.createFolds(pos, neg, 5);
		folds.get(0);
		//System.out.println(folds);
	}
}
