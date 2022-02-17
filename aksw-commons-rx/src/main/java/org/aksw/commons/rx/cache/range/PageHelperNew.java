package org.aksw.commons.rx.cache.range;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.function.LongSupplier;

import org.aksw.commons.lock.LockUtils;
import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;
import org.aksw.commons.util.range.RangeUtils;
import org.aksw.commons.util.ref.RefFuture;
import org.aksw.commons.util.slot.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;


/**
 * The class drives the iteration of items from the cache
 * and triggers fetching of data as necessary.
 *
 * Thereby this class does not fetch the data directly, but it declares
 * interest in data ranges. The SmartRangeCache will schedule loading of the region
 * at least as long as interest is expressed.
 *
 * @author raven
 *
 * @param <T>
 */
public abstract class PageHelperNew<T>
    extends AutoCloseableWithLeakDetectionBase
{
    private static final Logger logger = LoggerFactory.getLogger(PageHelperNew.class);

    protected SliceWithAutoSync<T> slice;
    protected SmartRangeCacheImpl<T> cache;
    protected SliceAccessor<T> pageRange;


    /** At a checkpoint the data fetching tasks for the next blocks are scheduled */
    protected long nextCheckpointOffset;


    public PageHelperNew(SmartRangeCacheImpl<T> cache, long nextCheckpointOffset) {
        super();
        this.cache = cache;
        this.slice = cache.getSlice();
        this.pageRange = slice.newSliceAccessor();
        this.nextCheckpointOffset = nextCheckpointOffset;
    }

    public long getNextCheckpointOffset() {
        return nextCheckpointOffset;
    }

    public SliceAccessor<T> getPageRange() {
        return pageRange;
    }

    /**
     * Schedule ensured loading of the next 'n' items since the last
     * checkpoint.
     *
     * Check whether there are any gaps ahead that require
     * scheduling requests to the backend
     *
     */
    protected void checkpoint(long n) {
      long start = nextCheckpointOffset;
      long end = start + n;

        Range<Long> claimAheadRange = Range.closedOpen(start, end);

        LockUtils.runWithLock(cache.getExecutorCreationReadLock(), () -> {
            slice.readMetaData(metaData -> {
                RangeSet<Long> gaps = metaData.getGaps(claimAheadRange);
                processGaps(gaps, start, end);
            });

            nextCheckpointOffset = end;
        });
    }

    @Override
    protected void closeActual() {
        pageRange.close();
    }


    private static final Logger logger = LoggerFactory.getLogger(PageHelperForConsumer.class);

    // This map holds a single client's requested slots across all tasked workers
    protected Map<RangeRequestWorker<T>, Slot<Long>> workerToSlot = new IdentityHashMap<>();

    // protected LongSupplier offsetSupplier;
    protected long maxRedundantFetchSize = 1000;

    public PageHelperNew(SmartRangeCacheImpl<T> cache, long nextCheckpointOffset, LongSupplier offsetSupplier) {
        this.cache = cache;
        this.slice = cache.getSlice();
        this.pageRange = slice.newSliceAccessor();
        this.nextCheckpointOffset = nextCheckpointOffset;
    }


    @Override
    public void checkpoint(long n) {
        clearPassedSlots();
        super.checkpoint(n);
    }

    public void clearPassedSlots() {
        Iterator<Slot<Long>> it = workerToSlot.values().iterator();
        while (it.hasNext()) {
            Slot<Long> slot = it.next();
            Long value = slot.getSupplier().get();
            // long currentOffset = offsetSupplier.getAsLong();
            long currentOffset = nextCheckpointOffset;
            if (value < currentOffset) {
                logger.info("Clearing slot for offset " + slot.getSupplier().get() + " because current offset " + currentOffset + " is higher");
                slot.close();
                it.remove();
            }
        }
    }


    protected void scheduleWorkerToGaps(RangeSet<Long> gaps) {

        // Index workers by offset
        // If multiple workers have the same offset then only pick the first one with the highest request range

        Map<Long, RangeRequestWorker<T>> offsetToWorker = new HashMap<>();
        NavigableMap<Long, Long> offsetToEndpoint = new TreeMap<>();



        // for (RangeRequestWorker<T> e : workerToSlot.keySet()) {
        for (RangeRequestWorker<T> e : cache.getExecutors()) {
            long workerStart = e.getCurrentOffset();
            long workerEnd = e.getEndOffset();

            // Skip workers that have reached their end of their data retrieval range
            // because we cannot ask those for additional data
            if (workerStart != workerEnd) {

                Long priorEndpoint = offsetToEndpoint.get(workerStart);
                if (priorEndpoint == null || priorEndpoint < workerEnd) {
                    offsetToWorker.put(workerStart, e);
                    offsetToEndpoint.put(workerStart, workerEnd);
                }
            }
        }

        NavigableMap<Long, Long> workerSchedules = RangeUtils.scheduleRangeSupply(offsetToEndpoint, gaps, maxRedundantFetchSize, cache.requestLimit);

        for (Entry<Long, Long> schedule : workerSchedules.entrySet()) {
            long start = schedule.getKey();
            long end = schedule.getValue();
            long length = end - start;

            RangeRequestWorker<T> worker = offsetToWorker.get(start);

            // If there is no worker with that offset then create one
            // Otherwise update its slot
            if (worker == null) {
                Entry<RangeRequestWorker<T>, Slot<Long>> workerAndSlot = cache.newExecutor(start, length);
                workerToSlot.put(workerAndSlot.getKey(), workerAndSlot.getValue());
            } else {
                Slot<Long> slot = workerToSlot.get(worker);

                if (slot == null) {
                    // We are reusing an existing worker; allocate a new slot on it
                    slot = worker.newDemandSlot();
                    workerToSlot.put(worker, slot);
                }

                slot.set(end);
            }


        }
    }

    protected void processGaps(RangeSet<Long> gaps, long start, long end) {
        scheduleWorkerToGaps(gaps);
    }

    protected void closeActual() {
        logger.debug("Releasing slots: " + workerToSlot);
        workerToSlot.values().forEach(Slot::close);
        workerToSlot.clear();

        super.closeActual();
    }
}

