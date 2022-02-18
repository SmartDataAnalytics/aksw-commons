package org.aksw.commons.rx.cache.range;

import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class RangeSetComplement<T extends Comparable<T>>
	implements RangeSet<T>
{
	protected RangeSet<T> rangeSet;
	protected Range<T> restriction;
	
	public RangeSetComplement(RangeSet<T> rangeSet, Range<T> restriction) {
		super();
		this.rangeSet = rangeSet;
		this.restriction = restriction;
	}

	@Override
	public boolean contains(T value) {
		return !rangeSet.contains(value);
	}
	
	@Override
	public Range<T> rangeContaining(T value) {
		Range<T> result = RangeSetUtils.getRangesBeforeAndAfter(this, value).stream().filter(range -> range.contains(value)).findFirst().orElse(null);
		return result;
	}
	
	@Override
	public boolean intersects(Range<T> otherRange) {
		return !subRangeSet(otherRange).isEmpty();
	}
	
	@Override
	public boolean encloses(Range<T> otherRange) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean enclosesAll(RangeSet<T> other) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isEmpty() {
		return !rangeSet.isEmpty();
	}
	
	@Override
	public Range<T> span() {
		Range<T> result;
		if (isEmpty()) {
			throw new NoSuchElementException();
		} else {
			Range<T> first = Iterables.getFirst(asRanges(), null);
			Range<T> last = Iterables.getFirst(asDescendingSetOfRanges(), null);
			result = first.span(last);
		}
		return result;
	}
	
	@Override
	public Set<Range<T>> asRanges() {
		return RangeSetUtils.complementAsRanges(rangeSet, restriction);
	}
	
	@Override
	public Set<Range<T>> asDescendingSetOfRanges() {
		return RangeSetUtils.complementAsDescendingSetOfRanges(rangeSet, restriction);
	}
	
	@Override
	public RangeSet<T> complement() {
		throw new UnsupportedOperationException();
		// FIXME Needs to consider restriction
		// return rangeSet;
	}
	
	@Override
	public RangeSet<T> subRangeSet(Range<T> view) {
		return new RangeSetComplement<>(rangeSet, view);
	}
	
	// FIXME Methods below need to validate against restriction
	
	@Override
	public void add(Range<T> range) {
		rangeSet.remove(range);
	}
	
	@Override
	public void remove(Range<T> range) {
		rangeSet.add(range);
	}
	
	@Override
	public void clear() {
		rangeSet.add(Range.all());
	}
	
	@Override
	public void addAll(RangeSet<T> other) {
		rangeSet.removeAll(other);
	}
	
	@Override
	public void removeAll(RangeSet<T> other) {
		rangeSet.addAll(other);
	}
	
	@Override
	public String toString() {
		return asRanges().toString();
	}
}
