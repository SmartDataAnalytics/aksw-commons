package org.aksw.commons.rx.cache.range;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

import org.aksw.commons.collections.PrefetchIterator;
import org.aksw.commons.util.range.Endpoint;
import org.aksw.commons.util.range.RangeUtils;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class RangeSetUtils {

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
	public static <T extends Comparable<T>> Set<Range<T>> complement(RangeSet<T> set, Range<T> restriction) {
		return new AbstractSet<Range<T>>() {

			@Override
			public Iterator<Range<T>> iterator() {
				return new AbstractIterator<Range<T>>() {
					Iterator<Range<T>> it = set.subRangeSet(restriction).asRanges().iterator();

					Endpoint<T> lastEndpoint = RangeUtils.getLowerEndpoint(restriction);
										
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
								ae = RangeUtils.getLowerEndpoint(a).toggleBoundType();								
								lastEndpoint = RangeUtils.getUpperEndpoint(a).toggleBoundType();
							} else { // (!it.hasNext())
								ae = RangeUtils.getUpperEndpoint(restriction);
								it = null;
							}
							
							if (!tmp.equals(ae)) {
								r = RangeUtils.create(tmp, ae);
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
