package org.aksw.commons.collection.rangeset;

import java.util.Set;

import org.aksw.commons.collections.ConvertingSet;
import org.aksw.commons.util.range.RangeUtils;

import com.google.common.base.Converter;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class ConvertingRangeSet<T extends Comparable<T>, U extends Comparable<U>>
    implements RangeSet<T>
{
    // Convert from wrapped view to public view
    protected Converter<U, T> endpointConverter;
    protected Converter<Range<U>, Range<T>> rangeConverter;

    protected RangeSet<U> backend;


    public static <I extends Comparable<I>, O extends Comparable<O>> Converter<Range<I>, Range<O>> createRangeConverter(Converter<I, O> endpointConverter) {
        return Converter.from(
               (Range<I> a) -> RangeUtils.map(a, endpointConverter::convert),
               (Range<O> b) -> RangeUtils.map(b, endpointConverter.reverse()::convert));
    }


    public ConvertingRangeSet(RangeSet<U> backend, Converter<U, T> endpointConverter) {
        this(backend, endpointConverter, createRangeConverter(endpointConverter));
    }


    public ConvertingRangeSet(RangeSet<U> backend, Converter<U, T> endpointConverter,
            Converter<Range<U>, Range<T>> rangeConverter) {
        super();
        this.backend = backend;
        this.endpointConverter = endpointConverter;
        this.rangeConverter = rangeConverter;
    }


    // public static T extends Comparable<T>, U extends Comparable<U> map()

    @Override
    public boolean contains(T value) {
        U v = endpointConverter.reverse().convert(value);
        boolean result = backend.contains(v);
        return result;
    }

    @Override
    public Range<T> rangeContaining(T value) {
        U v = endpointConverter.reverse().convert(value);
        Range<U> b = backend.rangeContaining(v);
        Range<T> result = rangeConverter.convert(b);
        return result;
    }

    @Override
    public boolean intersects(Range<T> otherRange) {
        Range<U> or = rangeConverter.reverse().convert(otherRange);
        boolean result = backend.intersects(or);
        return result;
    }

    @Override
    public boolean encloses(Range<T> otherRange) {
        Range<U> or = rangeConverter.reverse().convert(otherRange);
        boolean result = backend.encloses(or);
        return result;
    }

    @Override
    public boolean enclosesAll(RangeSet<T> other) {
        // TODO We could check which set is smaller and apply conversion accordingly
        boolean result = backend.enclosesAll(new ConvertingRangeSet<>(other, endpointConverter.reverse(), rangeConverter.reverse()));
        return result;
    }

    @Override
    public boolean isEmpty() {
        return backend.isEmpty();
    }

    @Override
    public Range<T> span() {
        Range<U> r = backend.span();
        Range<T> result = rangeConverter.convert(r);
        return result;
    }

    @Override
    public Set<Range<T>> asRanges() {
        return new ConvertingSet<>(backend.asRanges(), rangeConverter);
    }

    @Override
    public Set<Range<T>> asDescendingSetOfRanges() {
        return new ConvertingSet<>(backend.asDescendingSetOfRanges(), rangeConverter);
    }

    @Override
    public RangeSet<T> complement() {
        // TODO cache reference
        return new ConvertingRangeSet<>(backend.complement(), endpointConverter, rangeConverter);
    }

    @Override
    public RangeSet<T> subRangeSet(Range<T> view) {
        // TODO Auto-generated method stub
        Range<U> r = rangeConverter.reverse().convert(view);
        return new ConvertingRangeSet<>(backend.subRangeSet(r), endpointConverter, rangeConverter);
    }

    @Override
    public void add(Range<T> range) {
        Range<U> r = rangeConverter.reverse().convert(range);
        backend.add(r);
    }

    @Override
    public void remove(Range<T> range) {
        Range<U> r = rangeConverter.reverse().convert(range);
        backend.remove(r);
    }

    @Override
    public void clear() {
        backend.clear();
    }

    @Override
    public void addAll(RangeSet<T> other) {
        backend.addAll(new ConvertingRangeSet<>(other, endpointConverter.reverse(), rangeConverter.reverse()));
    }

    @Override
    public void removeAll(RangeSet<T> other) {
        backend.removeAll(new ConvertingRangeSet<>(other, endpointConverter.reverse(), rangeConverter.reverse()));
    }

}
