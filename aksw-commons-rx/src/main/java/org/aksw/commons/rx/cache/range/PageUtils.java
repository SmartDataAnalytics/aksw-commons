package org.aksw.commons.rx.cache.range;

import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class PageUtils {
	public static long getPageIndexForOffset(long offset, long pageSize) {
		return offset / pageSize;
	}
	
	public static long getIndexInPage(long offset, long pageSize) {
		return offset % pageSize;
	}

	public static int getIndexInPage(long offset, int pageSize) {
		return (int)(offset % (long)pageSize);
	}
	
	public static long getPageOffsetForId(long pageId, long pageSize) {
		return pageId * pageSize;
	}

	/** Return a stream of the page indices touched by the range w.r.t. the page size */
	public static LongStream touchedPageIndices(Range<Long> range, long pageSize) {
		ContiguousSet<Long> set = ContiguousSet.create(range, DiscreteDomain.longs());
		LongStream result = set.isEmpty()
				? LongStream.empty()
				: LongStream.rangeClosed(
						getPageIndexForOffset(set.first(), pageSize),
						getPageIndexForOffset(set.last(), pageSize));
		return result;
	}
	
	public static NavigableSet<Long> touchedPageIndices(RangeSet<Long> rangeSet, long pageSize) {
		NavigableSet<Long> result = rangeSet.asRanges().stream()
			.flatMapToLong(range -> PageUtils.touchedPageIndices(range, pageSize))
			.boxed()
			.collect(Collectors.toCollection(TreeSet::new));

		return result;
	}

}
