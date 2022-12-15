package org.aksw.commons.util.range;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.BoundType;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.google.common.math.LongMath;

public class RangeUtils {
    public static final Range<Long> rangeStartingWithZero = Range.atLeast(0l);
    public static final Range<Long> RANGE_LONG = Range.closed(Long.MIN_VALUE, Long.MAX_VALUE);

    public static long sizeLong(Range<Long> range) {
        Range<Long> r = range
                .intersection(RANGE_LONG)
                .canonical(DiscreteDomain.longs());

        long start = r.lowerEndpoint();
        long end = r.upperEndpoint();

        long result = DiscreteDomain.longs().distance(start, end);

        return result;
    }

    public static int intLength(Range<Long> range) {
        ContiguousSet<Long> set = ContiguousSet.create(range, DiscreteDomain.longs());
        int result = set.size();
        return result;
    }

    public static <T> List<T> subList(List<T> list, Range<Integer> subRange) {
        // Subrange's offset must be equal-or-greater-than 0

        Range<Integer> listRange = Range.lessThan(list.size());
        Range<Integer> effectiveRange = listRange.intersection(subRange);
        ContiguousSet<Integer> set = ContiguousSet.create(effectiveRange, DiscreteDomain.integers());
        List<T> result;

        if (set.isEmpty()) {
            result = Collections.emptyList();
        } else {
            int first = set.first();
            int last = set.last();
            result = list.subList(first, last + 1);
        }
        return result;
    }

    public static <T extends Comparable<T>> Optional<T> tryGetSingleton(Range<T> range) {
        boolean isSingleton = isSingleton(range);
        return Optional.ofNullable(isSingleton ? range.lowerEndpoint() : null);
    }


    /** True iff all ranges are singletons */
    public static boolean isDiscrete(RangeSet<?> rangeSet) {
        return isDiscrete(rangeSet.asRanges());
    }

    public static boolean isDiscrete(Collection<? extends Range<?>> ranges) {
        boolean result = ranges.stream().allMatch(RangeUtils::isSingleton);
        return result;
    }

    public static boolean isSingleton(RangeSet<?> rangeSet) {
        return isSingleton(rangeSet.asRanges());
    }

    public static boolean isSingleton(Collection<? extends Range<?>> ranges) {
        boolean result = ranges.size() == 1 && isSingleton(ranges.iterator().next());
        return result;
    }

    public static boolean isSingleton(Range<?> range) {
        boolean result =
                range.hasLowerBound() &&
                range.hasUpperBound() &&
                range.lowerBoundType().equals(BoundType.CLOSED) &&
                range.upperBoundType().equals(BoundType.CLOSED) &&
                Objects.equals(range.lowerEndpoint(), range.upperEndpoint());

        return result;
    }

    public static long pickLong(Range<Long> range, Random random) {
        Range<Long> norm = range.intersection(Range.closed(Long.MIN_VALUE, Long.MAX_VALUE));
        long l = norm.lowerEndpoint();
        long u = norm.upperEndpoint();
        double pick = random.nextDouble();
        long result = l + Math.round(pick * (u - l));
        return result;
    }

    public static double pickDouble(Range<Double> range, Random random) {
        Range<Double> norm = range.intersection(Range.closed(Double.MIN_VALUE, Double.MAX_VALUE));
        double l = norm.lowerEndpoint();
        double u = norm.upperEndpoint();
        double pick = random.nextDouble();
        double result = l + pick * (u - l);
        return result;
    }



    public static CountInfo toCountInfo(Range<? extends Number> range) {
        Long min = range.hasLowerBound() ? range.lowerEndpoint().longValue() : 0;
        Long max = range.hasUpperBound() ? range.upperEndpoint().longValue() : null;

        CountInfo result = new CountInfo(
                min,
                max == null ? true : !max.equals(min),
                max
        );
        return result;
    }

    /**
     * Convert a range relative within another one to an absolute range
     *
     * @param outer
     * @param relative
     * @param domain
     * @param addition
     * @return
     */
    public static <C extends Comparable<C>> Range<C> makeAbsolute(Range<C> outer, Range<C> relative, DiscreteDomain<C> domain, BiFunction<C, Long, C> addition) {
        long distance = domain.distance(outer.lowerEndpoint(), relative.lowerEndpoint());

        Range<C> shifted = RangeUtils.shift(relative, distance, domain, addition);
        Range<C> result = shifted.intersection(outer);
        return result;
    }


    /* Inefficient approach to shifting - don't use
    public static <C extends Comparable<C>> Range<C> shift(Range<C> range, long distance, DiscreteDomain<C> domain) {
        BiFunction<C, Long, C> addition = (item, d) -> {
            C result = item;
            if(d >= 0) {
                for(int i = 0; i < d; ++i) {
                    result = domain.next(item);
                }
            } else {
                for(int i = 0; i < -d; ++i) {
                    result = domain.previous(item);
                }
            }
            return result;
        };

        Range<C> result = shift(range, distance, domain, addition);
        return result;
    }
    */

    public static Range<Long> shiftLong(Range<Long> rawRange, long distance) {
        return map(rawRange, v -> v + distance);
    }

    public static <C extends Comparable<C>> Range<C> shift(Range<C> rawRange, long distance, DiscreteDomain<C> domain, BiFunction<C, Long, C> addition) {

        Range<C> range = rawRange.canonical(domain);

        Range<C> result;
        if(range.hasLowerBound()) {
            C oldLower = range.lowerEndpoint();
            C newLower = addition.apply(oldLower, distance);

            if(range.hasUpperBound()) {
                C oldUpper = range.upperEndpoint();
                C newUpper = addition.apply(oldUpper, distance);
                result = Range.closedOpen(newLower, newUpper);
            } else {
                result = Range.atLeast(oldLower);
            }

        } else {
            throw new IllegalArgumentException("Cannot displace a range without lower bound");
        }

        return result;
    }

    public static <K extends Comparable<K>, V> Set<Entry<Range<K>, V>> getIntersectingRanges(Range<K> r, Collection<Entry<Range<K>, V>> ranges) {
        Set<Entry<Range<K>, V>> result = ranges.stream()
            .filter(e -> !r.intersection(e.getKey()).isEmpty())
            .collect(Collectors.toSet());

        return result;
    }

    //public static NavigableMap<T extends Comparable> getOverlapping items

    public static Range<Long> startFromZero(Range<Long> range) {
        Range<Long> result = range.intersection(rangeStartingWithZero);
        return result;
    }

    /**
     * Apply a binary operator (e.g. multiplication, addition, ...) to any endpoint of the range and a given value.
     *
     * @param <I>
     * @param range
     * @param value
     * @param op
     * @return
     */
    public static <I extends Comparable<I>, O extends Comparable<O>, V> Range<O> apply(
            Range<I> range,
            V value,
        BiFunction<? super I, ? super V, ? extends O> op)
    {
        Range<O> result = map(range, endpoint -> op.apply(endpoint, value));

        return result;
    }

    /**
     * Return a new range with each concrete endpoint of the input range passed through a transformation function
     */
    public static <I extends Comparable<I>, O extends Comparable<O>> Range<O> map(
            Range<I> range,
            Function<? super I, ? extends O> mapper)
    {
        Range<O> result;

        if (range.hasLowerBound()) {
            if (range.hasUpperBound()) {
                result = Range.range(mapper.apply(range.lowerEndpoint()), range.lowerBoundType(), mapper.apply(range.upperEndpoint()), range.upperBoundType());
            } else {
                result = Range.downTo(mapper.apply(range.lowerEndpoint()), range.lowerBoundType());
            }
        } else {
            if (range.hasUpperBound()) {
                result = Range.upTo(mapper.apply(range.upperEndpoint()), range.upperBoundType());
            } else {
                result = Range.all();
            }
        }

        return result;
    }


    public static Range<Long> multiplyByPageSize(Range<Long> range, long pageSize) {
        Range<Long> result = apply(range, pageSize, (endpoint, value) -> endpoint * value);
        return result;

// The following code was converted to the method apply(range, value, op)
//
//      Range<Long> result;
//
//      if(range.hasLowerBound()) {
//          if(range.hasUpperBound()) {
//              result = Range.closedOpen(range.lowerEndpoint() * pageSize, range.upperEndpoint() * pageSize);
//          } else {
//              result = Range.atLeast(range.lowerEndpoint() * pageSize);
//          }
//      } else {
//          if(range.hasUpperBound()) {
//              result = Range.lessThan(range.upperEndpoint() * pageSize);
//          } else {
//              result = Range.all();
//          }
//
//      }

    }

    public static PageInfo<Long> computeRange(Range<Long> range, long pageSize) {
        // Example: If pageSize=100 and offset = 130, then we will adjust the offset to 100, and use a subOffset of 30
        long o = range.hasLowerBound() ? range.lowerEndpoint() : 0;

        long subOffset = o % pageSize;
        o -= subOffset;

        // Adjust the limit to a page boundary; the original limit becomes the subLimit
        // And we will extend the new limit to the page boundary again.
        // Example: If pageSize=100 and limit = 130, then we adjust the new limit to 200

        Range<Long> outerRange;
        Range<Long> innerRange;
        if(range.hasUpperBound()) {
            long limit = range.upperEndpoint() - range.lowerEndpoint();
            long l = limit;


            long mod = l % pageSize;
            long extra = mod != 0 ? pageSize - mod : 0;
            l += extra;

            outerRange = Range.closedOpen(o, o + l);
            innerRange = Range.closedOpen(subOffset, limit);
        } else {
            outerRange = Range.atLeast(o);
            innerRange = Range.atLeast(subOffset);
        }

        PageInfo<Long> result = new PageInfo<>(outerRange, innerRange);

        return result;
    }

    /**
     * Compute the set of gaps for the given request range.
     * This is the complement of the given ranges constrained to the request range.
     */
    public static <C extends Comparable<C>> RangeSet<C> gaps(Range<C> request, RangeSet<C> ranges) {
        RangeSet<C> gaps = ranges.complement().subRangeSet(request);
        return gaps;
    }



    /**
     * Given a map [start, end) pairs (start inclusive, end exclusive) of initial suppliers,
     * return a schedule that covers the set of gaps.
     *
     * @param <K>
     * @param offsetToKey
     * @param keyToMaxOffset // If there are multiple candidates with the same offset then only the one with the highest max offset should be chosen
     * @param demandGaps
     * @param maxRedundantSize The maximum number of consecutive items that may be fetched redundantly without enforcing a separate request.
     * @param maxSupplierRange The maximum number of items a single supplier can supply.
     */
    public static NavigableMap<Long, Long> scheduleRangeSupply(
            NavigableMap<Long, Long> supplyOffsetToEndpoint,
            RangeSet<Long> demandGaps,
            long maxRedundantSize,
            long maxSupplierRange) {

        // Copy the map; new allocations are tracked in it
        NavigableMap<Long, Long> offsetToEndpoint = new TreeMap<>(supplyOffsetToEndpoint);
        NavigableMap<Long, Long> result = new TreeMap<>();

        // MapUtils.union(null, null)

        // Preprocessing: Split all gaps by the worker's range endpoints (start and end)
        // This way we can assign complete sub-ranges to workers which should make the code simpler
        // because there is no need to handle splitting in the gap-worker-assignment algo

        Set<Long> allSplitPoints = new TreeSet<>();
        Set<Long> startPoints = offsetToEndpoint.keySet();
        Collection<Long> endPoints = offsetToEndpoint.values();
//        Set<Long> endPoints = offsetTo.values().stream()
//                .flatMap(key -> Optional.ofNullable(keyToMaxOffset.apply(key)).stream())
//                .collect(Collectors.toSet());

        allSplitPoints.addAll(startPoints);
        allSplitPoints.addAll(endPoints);

        // TODO Add the range offsets too?

        // Split all gaps by the executor offsets
        // Then assign each chunk to one of the executors
        RangeMap<Long, ?> gapRangeMap = RangeMapUtils.create(demandGaps.asRanges());


        RangeMapUtils.split(gapRangeMap, allSplitPoints);


        Set<Range<Long>> gapSet = gapRangeMap.asMapOfRanges().keySet();

        long bestOffset = -1;
        long bestMaxEnd = -1;
        long bestCurEnd = -1;


        for (Range<Long> gap : gapSet) {

            // We may need to schedule multiple workers if the gap is larger than a worker's maximum request range

            ContiguousSet<Long> tmp = ContiguousSet.create(gap, DiscreteDomain.longs());
            long gapStart = tmp.first();
            long gapEnd = LongMath.saturatedAdd(tmp.last(), 1);

            while (gapStart < gapEnd) {

                if (bestOffset >= 0) {
                    // Check if the gap's end point is covered by a worker
                    if (gapEnd < bestMaxEnd) {
                        // And the distance to the so far bestCurEnd must not exceed maxRedundantSize
                        long d = gapStart - bestCurEnd;

                        if (d <= maxRedundantSize) {
                            bestCurEnd = gapEnd;
                        } else {
                            result.put(bestOffset, bestCurEnd);
                            bestOffset = -1;
                        }
                    } else {
                        // The gap is outside of the worker range
                        // 2 Options:
                        // - Find another worker with a lower offset that covers the current range
                        // - Finalize the current bestWorker assignment and find or create a new worker
                        Long reallocateOffset = streamEnclosingRanges(offsetToEndpoint, bestOffset, gapEnd)
                            .findFirst().orElse(null);

                        if (reallocateOffset == null) {
                            // Finalize the current best match (if there is one)
                            if (bestOffset >= 0) {
                                result.put(bestOffset, bestCurEnd);
                            }

                            bestOffset = -1;

                        } else {
                            bestOffset = reallocateOffset;
                            bestMaxEnd = offsetToEndpoint.get(reallocateOffset);
                            // bestCurEnd = gapEnd;
                            bestCurEnd = Math.min(gapEnd, bestMaxEnd);
                        }

                    }
                }

                // Try to assign the gap to the current best worker
                if (bestOffset < 0) {
                    Long candOffset = streamEnclosingRanges(offsetToEndpoint, gapStart, gapEnd)
                            .findFirst().orElse(null);

                    if (candOffset != null) {
                        bestOffset = candOffset;
                        bestMaxEnd = offsetToEndpoint.get(candOffset);
                        bestCurEnd = Math.min(gapEnd, bestMaxEnd);

                    } else {
                        // Allocate a new worker based on the current gap
                        // int maxRequestLength = 0;
                        bestOffset = gapStart;
                        bestMaxEnd = LongMath.saturatedAdd(gapStart, maxSupplierRange); // -1; //gapStart + maxRequestLength;
                        // bestCurEnd = gapEnd;
                        bestCurEnd = Math.min(gapEnd, bestMaxEnd);
                    }
                }

                gapStart = bestCurEnd;
            }
        }


        if (bestOffset >= 0) {
            result.put(bestOffset, bestCurEnd);
        }


        boolean enableSanityCheck = true;
        if (enableSanityCheck) {
            RangeSet<Long> scheduledRanges = TreeRangeSet.create();
            for (Entry<Long, Long> e : result.entrySet()) {
                scheduledRanges.add(Range.closedOpen(e.getKey(), e.getValue()));
            }

            boolean isEveryGapCovered = scheduledRanges.enclosesAll(gapSet);
            if (!isEveryGapCovered) {
                throw new RuntimeException("Sanity check failed");
            }
        }
        return result;
    }


    /**
     * Streams all enclosing ranges (if any) ordered by decreasing offset
     * based on offsetToKey
     *
     * @param <K>
     * @param offsetToKey
     * @param keyToMaxOffset
     * @param start
     * @param end
     * @return
     */
    public static Stream<Long> streamEnclosingRanges(
            NavigableMap<Long, Long> offsetToEndpoint,
            long start,
            long end) {

        NavigableMap<Long, Long> headMap = offsetToEndpoint
                .headMap(start, true)
                // .tailMap(end - maxDistance, true)
                .descendingMap();


        Stream<Long> result = headMap.entrySet().stream()
            .filter(offsetAndEndpoint -> {
                long supplyEnd = offsetAndEndpoint.getValue();

                boolean isEnclosing = supplyEnd >= end;
                return isEnclosing;
            })
            .map(Entry::getKey);

        return result;
    }

    /** Create a range from limit and offset. Either or both may be null. */
    public static Range<Long> createRange(Long limit, Long offset) {
        long beginIndex = offset == null ? 0 : offset;
        Long endIndex = limit == null ? null : beginIndex + limit;

        Range<Long> result = endIndex == null
                ? Range.atLeast(beginIndex)
                : Range.closedOpen(beginIndex, endIndex)
                ;

        return result;
    }


    /**
     * Create method similar to {@link Range#range(Comparable, BoundType, Comparable, BoundType)}
     * with the difference that {@code null} can be used as values in order to denote 'below/above all'.
     * The respective bound type is then ignored.
     */
    public static <T extends Comparable<T>> Range<T> create(T lower, BoundType lowerType, T upper, BoundType upperType) {
        Range<T> result =
                lower == null
                    ? upper == null
                        ? Range.all()
                        : Range.upTo(upper, upperType)
                    : upper == null
                        ? Range.downTo(lower, lowerType)
                        : Range.range(lower, lowerType, upper, upperType);

        return result;
    }

    public static <T extends Comparable<T>> Range<T> create(Endpoint<T> lower, Endpoint<T> upper) {
        return create(lower.getValue(), lower.getBoundType(), upper.getValue(), upper.getBoundType());
    }

    public static <T extends Comparable<T>> Endpoint<T> getLowerEndpoint(Range<T> range) {
        Endpoint<T> result = range.hasLowerBound()
                ? new Endpoint<>(range.lowerEndpoint(), range.lowerBoundType())
                : new Endpoint<>(null, BoundType.OPEN);
        return result;
    }

    public static <T extends Comparable<T>> Endpoint<T> getUpperEndpoint(Range<T> range) {
        Endpoint<T> result = range.hasUpperBound()
                ? new Endpoint<>(range.upperEndpoint(), range.upperBoundType())
                : new Endpoint<>(null, BoundType.OPEN);
        return result;
    }


    /**
     * Compare the lower bounds of two <b>canonical</b> ranges.
     * This means the bound type is ignored.
     *
     * If both endpoints are absent they are considered equal and the result is 0
     */
    public static <C extends Comparable<C>> int compareToLowerBound(Range<C> a, Range<C> b) {
        return
            a.hasLowerBound()
                ? (b.hasLowerBound() ? a.lowerEndpoint().compareTo(b.lowerEndpoint()) : 1)
                : (b.hasLowerBound() ? -1 : 0);
    }


    /**
     * Compare the upper bounds of two <b>canonical</b> ranges.
     * This means the bound type is ignored.
     *
     * If both endpoints are absent they are considered equal and the result is 0
     */
    public static <C extends Comparable<C>> int compareToUpperBound(Range<C> a, Range<C> b) {
        return
            a.hasUpperBound()
                ? (b.hasUpperBound() ? a.upperEndpoint().compareTo(b.upperEndpoint()) : -1)
                : (b.hasUpperBound() ? 1 : 0);
    }


    /** Return a random double value within the given range. Open bounds are capped at Double.MIN_VALUE and MAX_VALUE,
     *  respectively.
     *  TODO This method currently does not respect exclusive bounds
     */
    public static double randomDouble(Range<? extends Number> range, Random random) {
        double min = range.hasLowerBound() ? range.lowerEndpoint().doubleValue() : Double.MIN_VALUE;
        double max = range.hasUpperBound() ? range.upperEndpoint().doubleValue() : Double.MAX_VALUE;

        double f = random.nextDouble();
        double result = min + (max - min) * f;

        return result;
    }
}
