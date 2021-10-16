package org.aksw.commons.util.range;

import java.util.Collection;
import java.util.Map.Entry;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

public class RangeMapUtils {

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
