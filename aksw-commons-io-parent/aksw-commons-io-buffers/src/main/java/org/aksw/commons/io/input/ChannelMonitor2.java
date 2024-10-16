package org.aksw.commons.io.input;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

public class ChannelMonitor2 {
    public class RangeTracker {
        protected long totalDurationNanos;
        // protected Set<Integer> readLengths;
        protected int minReadLength;
        protected int maxReadLength;
        protected long totalReadLength;
        protected long readCount;

        public RangeTracker() {
            this(Integer.MAX_VALUE, 0, 0, 0, 0);
        }

        public RangeTracker(int readLength, long totalDurationNanos) {
            this(readLength, readLength, readLength, totalDurationNanos, 1);
        }

        public RangeTracker(int minReadLength, int maxReadLength, long totalReadLength, long totalDurationNanos, long readCount) {
            super();
            this.totalDurationNanos = totalDurationNanos;
            this.minReadLength = minReadLength;
            this.maxReadLength = maxReadLength;
            this.totalReadLength = totalReadLength;
            this.readCount = readCount;
        }

        public long getTotalDurationNanos() {
            return totalDurationNanos;
        }

        public int getMinReadLength() {
            return minReadLength;
        }

        public long getTotalReadLength() {
            return totalReadLength;
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
            this.totalReadLength += contrib.totalReadLength;
            ++this.readCount;
        }

        @Override
        protected RangeTracker clone() {
            return new RangeTracker(minReadLength, maxReadLength, totalReadLength, totalDurationNanos, readCount);
        }
    }

    protected volatile NavigableMap<Long, RangeTracker> trackedReads = new TreeMap<>();
    protected AtomicLong readCounter = new AtomicLong();
    protected AtomicLong readAmount = new AtomicLong();

    public void addReadAmount(long readAmount) {
        this.readAmount.addAndGet(readAmount);
    }

    public void incReadCounter() {
        this.readCounter.addAndGet(1);
    }

    public long getReadCounter() {
        return readCounter.get();
    }

    public long getReadAmount() {
        return readAmount.get();
    }

    public NavigableMap<Long, RangeTracker> getTrackedReads() {
        return trackedReads;
    }

    public synchronized void submitReadStats(long offset, long readStartPos, long readEndPos, int readLength, long durationNanos) {
        if (readLength > 0) {  // Skip lengths that are <= 0
            RangeTracker tracker = trackedReads.computeIfAbsent(offset, o -> new RangeTracker());
            RangeTracker contrib = new RangeTracker(readLength, durationNanos);
            tracker.add(contrib);
        }
    }

    public void dumpJson(OutputStream out) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (JsonWriter w = gson.newJsonWriter(new OutputStreamWriter(out))) {
            w.beginArray();
            for (Entry<Long, RangeTracker> e : getTrackedReads().entrySet()) {
                w.beginObject();
                w.name("offset");
                w.value(e.getKey());
                w.name("length");
                w.value(e.getValue().getTotalReadLength());
                w.endObject();
            }
            w.endArray();
        }
    }

}
