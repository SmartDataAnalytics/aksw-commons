package org.aksw.commons.util.range;

import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

public class RangeMapUtils {
    /** See {@link #merge(RangeMap, Range, Collection, Supplier)}. */
    public static <K extends Comparable<K>, V, C extends Collection<V>> void merge(RangeMap<K, C> rangeMap, Range<K> range, V value, Supplier<C> newCollection) {
        merge(rangeMap, range, Collections.singleton(value), newCollection);
    }

    /**
     * Merge a given collection of values into the specified range.
     * All existing entries are remapped to fresh collections with these values appended,
     * whereas all "gaps" are mapped to the same fresh copy of the given values.
     */
    public static <K extends Comparable<K>, V, C extends Collection<V>> void merge(RangeMap<K, C> rangeMap, Range<K> range, Collection<V> values, Supplier<C> newCollection) {
        C copy = newCollection.get();
        copy.addAll(values);
        rangeMap.merge(range, copy, (priorValues, additions) -> {
            C r = newCollection.get();
            r.addAll(priorValues);
            r.addAll(additions);
            return r;
        });
    }

    /**
     * Create a RangeMap with dummy values from a set of ranges.
     *
     * @param <K>
     * @param ranges
     * @return
     */
    protected static <K extends Comparable<K>> RangeMap<K, ?> create(Collection<Range<K>> ranges) {
        RangeMap<K, Object> result = TreeRangeMap.create();
        Integer nothing = 0;
        ranges.forEach(range -> result.put(range, nothing));
        return result;
    }

    public static <K extends Comparable<K>, V> void split(RangeMap<K, V> ranges, Iterable<K> splitPoints) {
        for (K splitPoint : splitPoints) {
            split(ranges, splitPoint);
        }
    }

    public static <K extends Comparable<K>, V> void split(RangeMap<K, V> ranges, K splitPoint) {
        Entry<Range<K>, V> e = ranges.getEntry(splitPoint);
        if (e != null) {
            Range<K> before = e.getKey();
            V value = e.getValue();

            Range<K> lhs = before.intersection(Range.lessThan(splitPoint));
            Range<K> rhs = before.intersection(Range.atLeast(splitPoint));

            if (lhs.isEmpty() || rhs.isEmpty()) {
                // nothing to do - the split point matches the range's start or end
            } else {
                ranges.remove(before);
                ranges.put(lhs, value);
                ranges.put(rhs, value);
            }
        }
    }


}
