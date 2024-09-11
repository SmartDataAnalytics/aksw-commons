package org.aksw.commons.io.input;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;

public class ChannelMonitor {
    public class RangeTracker {
        protected long totalDurationNanos;
        // protected Set<Integer> readLengths;
        protected int minReadLength;
        protected int maxReadLength;
        // protected long totalReadlength;
        protected long readCount;

        public RangeTracker(int readLength, long totalDurationNanos) {
            this(readLength, readLength, totalDurationNanos, 1);
        }

        public RangeTracker(int minReadLength, int maxReadLength, long totalDurationNanos, long readCount) {
            super();
            this.totalDurationNanos = totalDurationNanos;
            this.minReadLength = minReadLength;
            this.maxReadLength = maxReadLength;
            this.readCount = readCount;
        }

        public long getTotalDurationNanos() {
            return totalDurationNanos;
        }

        public int getMinReadLength() {
            return minReadLength;
        }

        public int getMaxReadLength() {
            return maxReadLength;
        }

        public long getReadCount() {
            return readCount;
        }

        public void add(RangeTracker contrib) {
            this.totalDurationNanos += contrib.totalDurationNanos;
            this.maxReadLength = Math.max(this.maxReadLength, contrib.maxReadLength);
            this.minReadLength = this.minReadLength == -1
                    ? contrib.minReadLength
                    : Math.min(this.minReadLength, contrib.minReadLength);
            ++this.readCount;
        }

        @Override
        protected RangeTracker clone() {
            return new RangeTracker(minReadLength, maxReadLength, totalDurationNanos, readCount);
        }
    }

    protected RangeMap<Long, RangeTracker> trackedReads;

    public ChannelMonitor() {
        this.trackedReads = TreeRangeMap.create();
    }

    public RangeMap<Long, RangeTracker> getTrackedReads() {
        return trackedReads;
    }

    public synchronized void submitReadStats(long readStartPos, long readEndPos, int readLength, long durationNanos) {
        if (readLength > 0) {  // Skip lengths that are <= 0
            RangeTracker contribution = new RangeTracker(readLength, readLength, durationNanos, 1);

            Range<Long> span = Range.openClosed(readStartPos, readEndPos);

            RangeMap<Long, RangeTracker> subMap = trackedReads.subRangeMap(span);
            subMap.asMapOfRanges().values().forEach(tracker -> tracker.add(contribution));

            RangeSet<Long> rangeSet = TreeRangeSet.create(subMap.asMapOfRanges().keySet());
            RangeSet<Long> gaps = rangeSet.complement().subRangeSet(span);
            gaps.asRanges().forEach(range -> {
                trackedReads.put(range, contribution.clone());
            });
        }
    }
}
