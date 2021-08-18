package org.aksw.commons.rx.cache.range;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;

import org.aksw.commons.util.range.RangeUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;


/**
 * Metadata for slices of data.
 *
 * Holds information about
 * <ul>
 *   <li>the min/max number of known items</li>
 *   <li>loaded data ranges</li>
 *   <li>failed data ranges</li>
 * </ul>
 *
 * @author raven
 *
 */
public interface SliceMetaData {
    RangeSet<Long> getLoadedRanges();
    RangeMap<Long, List<Throwable>> getFailedRanges();

    long getMinimumKnownSize();
    long getMaximumKnownSize();

    SliceMetaData setMinimumKnownSize(long size);
    SliceMetaData setMaximumKnownSize(long size);

    default SliceMetaData setKnownSize(long size) {
        Preconditions.checkArgument(size >= 0, "Negative known size");

        setMinimumKnownSize(size);
        setMaximumKnownSize(size);

        return this;
    }

    /** A lock to control concurrent access to this object */
    ReadWriteLock getReadWriteLock();
    Condition getHasDataCondition();

    /** -1 If not exactly known */
    default long getKnownSize() {
        boolean isExact = isExactSizeKnown();
        long result = isExact ? getMaximumKnownSize() : -1;
        return result;
    }

    default RangeSet<Long> getGaps(Range<Long> requestRange) {
        RangeSet<Long> loadedRanges = getLoadedRanges();
        RangeSet<Long> result = RangeUtils.gaps(requestRange, loadedRanges);
        return result;
    }

    default boolean isExactSizeKnown() {
        long minSize = getMinimumKnownSize();
        long maxSize = getMaximumKnownSize();

        boolean result = minSize == maxSize;
        return result;
    }

    /**
     * Whether all data has been loaded. This is the case if
     * the exact size is known and there is only a single range covering
     * [0, maxSize)
     *
     * @return
     */
    default boolean isComplete() {
        boolean result = false;

        boolean isExactSizeKnown = isExactSizeKnown();

        if (isExactSizeKnown) {
            long exactSize = getMaximumKnownSize();

            RangeSet<Long> ranges = getLoadedRanges();
            Set<Range<Long>> set = ranges.asRanges();

            if (set.size() == 1) {
                Range<Long> range = set.iterator().next();
                ContiguousSet<Long> cs = ContiguousSet.create(range, DiscreteDomain.longs());
                Long start = cs.first();
                Long end = cs.last();

                result = start != null && end != null && start == 0l && (end + 1) == exactSize;
            }
        }

        return result;
    }
}
