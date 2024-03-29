package org.aksw.commons.collection.rangeset;

import com.google.common.base.Converter;
import com.google.common.collect.RangeSet;

/**
 * Operators that return actual RangeSet instances.
 * RangeSetUtils is only for Set views.
 */
public class RangeSetOps {

    public static <T extends Comparable<T>> RangeSetUnion<T> union(RangeSet<T> first, RangeSet<T> second) {
        return new RangeSetUnion<>(first, second);
    }

    public static <I extends Comparable<I>, O extends Comparable<O>> RangeSet<O> convert(RangeSet<I> rangeSet, Converter<I, O> endpointConverter) {
        return new ConvertingRangeSet<>(rangeSet, endpointConverter);
    }


    /** Create a view of a shifted range set of longs where each endpoint has a constant (possibly negative) value added to it */
    public static RangeSet<Long> shiftLong(RangeSet<Long> rangeSet, long shiftValue) {
        return convert(rangeSet, Converter.from((Long v) -> v + shiftValue, (Long v) -> v - shiftValue));
    }
}
