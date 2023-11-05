package org.aksw.commons.util.range;

import com.google.common.collect.BoundType;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

public class LongRanges {
    /**
     * Transform a range w.r.t. a discrete domain such that any lower bound is closed and the upper bound
     * is open. As a result, a zero-length range is represented by [x..x)
     *
     * @param <T>
     * @param range
     * @param domain
     * @return
     */
    // This seems to be the same as range.canonical(discreteDomain)
//    public static <T extends Comparable<T>> Range<T> makeClosedOpen(Range<T> range, DiscreteDomain<T> domain) {
//        ContiguousSet<T> set = ContiguousSet.create(range, domain);
//        range.canonical(domain)
//        Range<T> result = set.isEmpty()
//                ? Range.closedOpen(range.lowerEndpoint(), range.lowerEndpoint())
//                : ContiguousSet.create(range, domain).range(BoundType.CLOSED, BoundType.OPEN);
//        return result;
//    }

    public static <T extends Comparable<T>> T closedLowerEndpointOrNull(Range<T> range, DiscreteDomain<T> domain) {
        T result = !range.hasLowerBound()
                ? null
                : range.lowerBoundType().equals(BoundType.CLOSED)
                    ? range.lowerEndpoint()
                    : domain.next(range.lowerEndpoint());

        return result;
    }

    public static <T extends Comparable<T>> T openUpperEndpointOrNull(Range<T> range, DiscreteDomain<T> domain) {
        T result = !range.hasUpperBound()
                ? null
                : range.upperBoundType().equals(BoundType.CLOSED)
                    ? domain.next(range.upperEndpoint())
                    : range.upperEndpoint();

        return result;
    }

    public static Long rangeToOffset(Range<Long> range) {
        Long tmp = range == null
                ? null
                : closedLowerEndpointOrNull(range, DiscreteDomain.longs());

        Long result = tmp == null || tmp == 0 ? null : tmp;
        return result;
    }

    /**
     *
     * @param range
     * @return
     */
    public static Long rangeToLimit(Range<Long> range) {
        // range = range == null ? null : makeClosedOpen(range, DiscreteDomain.longs());
        range = range == null ? null : range.canonical(DiscreteDomain.longs());

        Long result = range == null || !range.hasUpperBound()
            ? null
            : DiscreteDomain.longs().distance(range.lowerEndpoint(), range.upperEndpoint())
                // If the upper bound is closed such as [x, x] then the result is the distance plus 1
                + (range.upperBoundType().equals(BoundType.CLOSED) ? 1 : 0);

        return result;
    }
}
