package org.aksw.commons.rx.cache.range;

import java.util.Set;

import com.google.common.collect.ForwardingSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public interface RangeSetDelegate<T extends Comparable<T>>
	extends RangeSet<T>
{
	RangeSet<T> getDelegate();

	@Override
	default boolean contains(T value) {
		return getDelegate().contains(value);
	}

	@Override
	default Range<T> rangeContaining(T value) {
		return getDelegate().rangeContaining(value);
	}

	@Override
	default boolean intersects(Range<T> otherRange) {
		return getDelegate().intersects(otherRange);
	}

	@Override
	default boolean encloses(Range<T> otherRange) {
		return getDelegate().encloses(otherRange);
	}

	@Override
	default boolean enclosesAll(RangeSet<T> other) {
		return getDelegate().enclosesAll(other);
	}

	@Override
	default boolean isEmpty() {
		return getDelegate().isEmpty();
	}

	@Override
	default Range<T> span() {
		return getDelegate().span();
	}

	@Override
	default Set<Range<T>> asRanges() {
		return new ForwardingSet<Range<T>>() {
			@Override
			protected Set<Range<T>> delegate() {
				return RangeSetDelegate.this.getDelegate().asRanges();
			}
		};
	}

	@Override
	default Set<Range<T>> asDescendingSetOfRanges() {
		return new ForwardingSet<Range<T>>() {
			@Override
			protected Set<Range<T>> delegate() {
				return RangeSetDelegate.this.getDelegate().asDescendingSetOfRanges();
			}
		};
	}

	@Override
	default RangeSet<T> complement() {
		return new RangeSetDelegate<T>() {
			@Override
			public RangeSet<T> getDelegate() {
				return RangeSetDelegate.this.getDelegate().complement();
			}
		};
	}

	@Override
	default RangeSet<T> subRangeSet(Range<T> view) 
	{
		return new RangeSetDelegate<T>() {
			@Override
			public RangeSet<T> getDelegate() {
				return RangeSetDelegate.this.getDelegate().subRangeSet(view);
			}
		};
	}

	@Override
	default void add(Range<T> range) {
		getDelegate().add(range);
	}

	@Override
	default void remove(Range<T> range) {
		getDelegate().remove(range);
	}

	@Override
	default void clear() {
		getDelegate().clear();
	}

	@Override
	default void addAll(RangeSet<T> other) {
		getDelegate().addAll(other);
	}

	@Override
	default void removeAll(RangeSet<T> other) {
		getDelegate().removeAll(other);
	}
}
