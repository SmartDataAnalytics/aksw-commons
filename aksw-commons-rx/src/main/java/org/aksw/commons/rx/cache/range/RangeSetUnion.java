package org.aksw.commons.rx.cache.range;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.util.range.RangeUtils;
import org.checkerframework.checker.units.qual.C;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class RangeSetUnion<T extends Comparable<T>>
	implements RangeSet<T>, Serializable
{
	private static final long serialVersionUID = 1L;

	protected RangeSet<T> first;
	protected RangeSet<T> second;
	
	
	protected transient RangeSet<T> complement = null;
	protected transient Set<Range<T>> asRanges = null;
	protected transient Set<Range<T>> asDescendingSetOfRanges = null;

	
	public RangeSetUnion(RangeSet<T> first, RangeSet<T> second) {
		super();
		this.first = first;
		this.second = second;
	}
	

	/** Prefer {@link RangeSetOps#union(RangeSet, RangeSet)} */
	public static <T extends Comparable<T>> RangeSetUnion<T> create(RangeSet<T> first, RangeSet<T> second) {
		return new RangeSetUnion<>(first, second);
	}
	
	public RangeSet<T> getFirst() {
		return first;
	}
	
	public RangeSet<T> getSecond() {
		return second;
	}
	
	@Override
	public boolean contains(T value) {
		boolean result = first.contains(value) || second.contains(value);
		return result;		
	}
	
	@Override
	public Range<T> rangeContaining(T value) {
		Range<T> result = RangeSetUtils.getRangesBeforeAndAfter(this, value).stream().filter(range -> range.contains(value)).findFirst().orElse(null);
		return result;
	}		
//		// Get the ranges before and after the value (if any)
//		Range<T> before = Iterables.getFirst(subRangeSet(Range.atMost(value)).asDescendingSetOfRanges(), null);
//		Range<T> after =  Iterables.getFirst(subRangeSet(Range.atLeast(value)).asRanges(), null);
//		
//		// If the value is contained in both ranges then those ranges are connected and we create the span
//		Range<T> result = null;
//		if (before != null && before.contains(value)) {
//			result = before;
//		}
//		
//		if (after != null && after.contains(value)) {
//			result = result == null ? after : result.span(after);
//		}
//		return result;

	@Override
	public boolean intersects(Range<T> otherRange) {
		// boolean result = !subRangeSet(otherRange).isEmpty();
		boolean result = first.intersects(otherRange) || second.intersects(otherRange);
		return result;		
	}

	@Override
	public boolean encloses(Range<T> otherRange) {
		T endpoint = otherRange.hasLowerBound()
				? otherRange.lowerEndpoint()
				: otherRange.hasUpperBound()
					? otherRange.upperEndpoint()
					: null;

		boolean result;
		if (endpoint == null) {
			// Get the first range (if it exists) and check whether it covers everything
			Range<T> onlyRange = Iterables.getFirst(asRanges(), null);
			result = onlyRange != null && onlyRange.encloses(otherRange);
		} else {
			result = RangeSetUtils.getRangesBeforeAndAfter(this, endpoint).stream().anyMatch(range -> range.encloses(otherRange));
		}
		return result;
	}

	@Override
	public boolean enclosesAll(RangeSet<T> other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		boolean result = first.isEmpty() && second.isEmpty();
		return result;
	}

	@Override
	public Range<T> span() {
		Range<T> result = first.span().span(second.span());
		return result;
	}

	@Override
	public Set<Range<T>> asRanges() {
		if (asRanges == null) {
			asRanges = new AsRangesBase<T>(
					SetUtils.newForwardingSet(first::asRanges),
					SetUtils.newForwardingSet(second::asRanges),
					RangeUtils::compareToLowerBound);
		}
		return asRanges;
	}

	@Override
	public Set<Range<T>> asDescendingSetOfRanges() {
		if (asDescendingSetOfRanges == null) {
			Comparator<Range<T>> cmp = RangeUtils::compareToUpperBound;
			cmp = cmp.reversed();
			asDescendingSetOfRanges = new AsRangesBase<T>(
					SetUtils.newForwardingSet(first::asDescendingSetOfRanges),
					SetUtils.newForwardingSet(second::asDescendingSetOfRanges),
					cmp);
		}
		return asDescendingSetOfRanges;
	}

	@Override
	public RangeSet<T> complement() {
		if (complement == null) {
			complement = new RangeSetComplement<>(this, Range.all());
		}
		
		return complement; 
	}

	@Override
	public RangeSet<T> subRangeSet(Range<T> view) {
		return new RangeSetUnion<>(
			first.subRangeSet(view),
			second.subRangeSet(view));
	}

	@Override
	public void add(Range<T> range) {
		first.add(range);
	}

	@Override
	public void remove(Range<T> range) {
		first.remove(range);
	}

	@Override
	public void clear() {
		first.clear();
	}

	@Override
	public void addAll(RangeSet<T> other) {
		first.addAll(other);
	}

	@Override
	public void removeAll(RangeSet<T> other) {
		first.removeAll(other);
	}

	@Override
	public String toString() {
		return asRanges().toString();
	}
}
