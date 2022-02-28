package org.aksw.commons.rx.cache.range;

import java.util.List;

import org.aksw.commons.util.range.RangeUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public interface SliceMetaDataBasic {
    RangeSet<Long> getLoadedRanges();
    RangeMap<Long, List<Throwable>> getFailedRanges();

    long getMinimumKnownSize();
    void setMinimumKnownSize(long size);

    long getMaximumKnownSize();
    void setMaximumKnownSize(long size);


    /** Updates the maximum known size iff the argument is less than the current known maximum */
    default SliceMetaDataBasic updateMaximumKnownSize(long size) {
        long current = getMaximumKnownSize();

        if (size < current) {
            setMaximumKnownSize(size);
        }

        return this;
    }

    /** Updates the minimum known size iff the argument is greater than the current known minimum */
    default SliceMetaDataBasic updateMinimumKnownSize(long size) {
        long current = getMinimumKnownSize();

        if (size > current) {
            setMinimumKnownSize(size);
        }

        return this;
    }


    default long getKnownSize() {
        long minSize = getMinimumKnownSize();
        long maxSize = getMaximumKnownSize();

        return minSize == maxSize ? minSize : -1;
    }

    default SliceMetaDataBasic setKnownSize(long size) {
        Preconditions.checkArgument(size >= 0, "Negative known size");

        setMinimumKnownSize(size);
        setMaximumKnownSize(size);

        return this;
    }

    // RangeSet<Long> getGaps(Range<Long> requestRange);

    default RangeSet<Long> getGaps(Range<Long> requestRange) {
        long maxKnownSize = getMaximumKnownSize();
        Range<Long> maxKnownRange = Range.closedOpen(0l, maxKnownSize);

        boolean isConnected = requestRange.isConnected(maxKnownRange);

        RangeSet<Long> result;
        if (isConnected) {
            Range<Long> effectiveRequestRange = requestRange.intersection(maxKnownRange);
            RangeSet<Long> loadedRanges = getLoadedRanges();
            result = RangeUtils.gaps(effectiveRequestRange, loadedRanges);
        } else {
            result = TreeRangeSet.create();
        }

        return result;
    }

}
