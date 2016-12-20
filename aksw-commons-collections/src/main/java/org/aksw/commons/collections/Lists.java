package org.aksw.commons.collections;

import java.util.Collections;
import java.util.List;

public class Lists {

	public static <T> List<T> accessDistributedPartition(List<T> items, int n, int i) {
    	// https://github.com/google/guava/issues/451
    	// The first r sublists of ls are of size q + 1, the rest (that is n - r sublists) are of size q

		int q = items.size() / n;
    	int r = items.size() % n;

		int s = q + 1;

		List<T> result;
		if(i < r) {
			int o = i * s;
			result = items.subList(o, o + s);
		} else if(i < n) {
			int o = r * s + (i - r) * q;
			result = items.subList(o, o + q);
		} else {
			result = Collections.emptyList();
		}

		return result;
	}

	/**
	 *
	 * @param list
	 * @param n Number of partitions
	 * @return
	 */
	public static <T> List<List<T>> distribute(List<T> list, int n) {
		List<List<T>> result = new ListLazy<List<T>>((i) -> accessDistributedPartition(list, n, i), n);
		return result;
	}
}
