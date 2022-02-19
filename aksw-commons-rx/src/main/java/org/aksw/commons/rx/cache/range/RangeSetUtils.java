package org.aksw.commons.rx.cache.range;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.commons.collections.PrefetchIterator;
import org.aksw.commons.util.range.Endpoint;
import org.aksw.commons.util.range.RangeUtils;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class RangeSetUtils {


	/**
	 * Returs a collection (list) of the range before and after the value (if they exist).
	 * Should both ranges exist and connect on the value then the span is created resulting in the range surrounding the value.
	 * @param value
	 * @return
	 */
	public static <T extends Comparable<T>> Collection<Range<T>> getRangesBeforeAndAfter(RangeSet<T> rangeSet, T value) {
		Range<T> before = Iterables.getFirst(rangeSet.subRangeSet(Range.atMost(value)).asDescendingSetOfRanges(), null);
		Range<T> after =  Iterables.getFirst(rangeSet.subRangeSet(Range.atLeast(value)).asRanges(), null);

		Collection<Range<T>> result;
		if (before == null) {
			if (after == null) {
				result = Collections.emptyList();
			} else {
				result = Collections.singleton(after);
			}
		} else {
			if (after == null) {
				result = Collections.singleton(before);
			} else {
				if (before.isConnected(after)) {
					result = Collections.singletonList(before.span(after));
				} else {
					result = ImmutableList.<Range<T>>builder().add(before).add(after).build();
				}				
			}
		}

		return result;
	}

	/** Return a set view of the differences; for every range of a remove all segments that overlap with b */
	public static <T extends Comparable<T>> Set<Range<T>> difference(RangeSet<T> aset, RangeSet<T> bset) {
		
		return new AbstractSet<Range<T>>() {
			@Override
			public Iterator<Range<T>> iterator() {
				return new PrefetchIterator<Range<T>>() {

					Iterator<Range<T>> ait = aset.asRanges().iterator();
					RangeSet<T> bComplement = bset.complement();

					@Override
					protected Iterator<Range<T>> prefetch() {
						Iterator<Range<T>> r = null;
						if (ait.hasNext()) {
							Range<T> a = ait.next();
							
							r = bComplement.subRangeSet(a).asRanges().iterator();							
						}
						
						return r;
					}
				};
			}
			@Override
			public int size() {
				return Iterators.size(iterator());
			}
		};
	}

	public static <T extends Comparable<T>> Set<Range<T>> symmetricDifference(RangeSet<T> aset, RangeSet<T> bset) {
		Set<Range<T>> added = difference(aset, bset);
		Set<Range<T>> removed = difference(bset, aset);
		return new AsRangesBase<>(added, removed, RangeUtils::compareToLowerBound);
	}
	
	/**
	 * Create an iterable over the complement of a range set w.r.t. a restriction range.
	 * Although the result is a set, all operations require iteration.
	 * 
	 * @param <T>
	 * @param set
	 * @param restriction
	 * @return
	 */
	public static <T extends Comparable<T>> Set<Range<T>> complementAsRanges(RangeSet<T> set, Range<T> restriction) {
		return complement(
				() -> set.subRangeSet(restriction).asRanges().iterator(),
				RangeUtils::getLowerEndpoint,
				RangeUtils::getUpperEndpoint,
				RangeUtils::create,
				restriction);
	}

	public static <T extends Comparable<T>> Set<Range<T>> complementAsDescendingSetOfRanges(RangeSet<T> set, Range<T> restriction) {
		return complement(
				() -> set.subRangeSet(restriction).asDescendingSetOfRanges().iterator(),
				RangeUtils::getUpperEndpoint,
				RangeUtils::getLowerEndpoint,
				(hi, lo) -> RangeUtils.create(lo, hi),
				restriction);
	}

	public static <T extends Comparable<T>> Set<Range<T>> complement(
			Supplier<Iterator<Range<T>>> iteratorSupp,
			Function<Range<T>, Endpoint<T>> getNearerEndpoint,
			Function<Range<T>, Endpoint<T>> getFartherEndpoint,
			BiFunction<Endpoint<T>, Endpoint<T>, Range<T>> createRange,
			Range<T> restriction) {
		return new AbstractSet<Range<T>>() {

			@Override
			public Iterator<Range<T>> iterator() {
				return new AbstractIterator<Range<T>>() {
					Iterator<Range<T>> it = iteratorSupp.get();

					Endpoint<T> lastEndpoint = getNearerEndpoint.apply(restriction);
										
					@Override
					protected Range<T> computeNext() {
						Range<T> r = null;
						for(;;) {
							Endpoint<T> ae;
							Endpoint<T> tmp = lastEndpoint;

							// When the last range of 'it' is consumed we may need to emit one more item
							// For this extra iteration we set 'it' to null
							if (it == null) {
								r = endOfData();
								break;
							} else if (it.hasNext()) {
								Range<T> a = it.next();
								ae = getNearerEndpoint.apply(a).toggleBoundType();								
								lastEndpoint = getFartherEndpoint.apply(a).toggleBoundType();
							} else { // (!it.hasNext())
								ae = getFartherEndpoint.apply(restriction);
								it = null;
							}
							
							if (!tmp.equals(ae) || it == null) {
								r = createRange.apply(tmp, ae);
								break;
							}
						}
						return r;
					}
				};
			}
			
			@Override
			public int size() {
				return Iterators.size(iterator());
			}
		};
	}
}
