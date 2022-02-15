package org.aksw.commons.rx.cache.range;

import com.google.common.collect.RangeSet;

public class RangeSetOps {

	public static <T extends Comparable<T>> RangeSetUnion<T> union(RangeSet<T> first, RangeSet<T> second) {
		return new RangeSetUnion<>(first, second);
	}

}
