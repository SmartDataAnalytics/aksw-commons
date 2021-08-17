package org.aksw.commons.rx.cache.range;

import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.LongSupplier;

import org.aksw.commons.util.range.RangeUtils;
import org.aksw.commons.util.slot.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;


public class PageHelperForConsumer<T>
    extends PageHelperBase<T>
{
    private static final Logger logger = LoggerFactory.getLogger(PageHelperForConsumer.class);

    protected Map<RangeRequestWorker<T>, Slot<Long>> workerToSlot = new IdentityHashMap<>();

    protected LongSupplier offsetSupplier;
    protected SmartRangeCacheImpl<T> cache;

    public PageHelperForConsumer(SmartRangeCacheImpl<T> cache, long nextCheckpointOffset, LongSupplier offsetSupplier) {
        super(cache.getSlice(), nextCheckpointOffset);
        this.offsetSupplier = offsetSupplier;
        this.cache = cache;
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
            long currentOffset = offsetSupplier.getAsLong();
            if (value < currentOffset) {
                logger.info("Clearing slot with value " + slot.getSupplier().get() + " because offset " + currentOffset + "is higher ");
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

        for (RangeRequestWorker<T> e : workerToSlot.keySet()) {
            long workerStart = e.getCurrentOffset();
            long workerEnd = e.getEndOffset();

            Long priorEndpoint = offsetToEndpoint.get(workerStart);
            if (priorEndpoint == null || priorEndpoint < workerEnd) {
                offsetToWorker.put(workerStart, e);
                offsetToEndpoint.put(workerStart, workerEnd);
            }
        }

        long maxRedundantFetchSize = 1000;
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
                slot.set(end);
            }


        }
    }

    @Override
    protected void processGaps(RangeSet<Long> gaps, long start, long end) {
        scheduleWorkerToGaps(gaps);
    }

    @Override
    protected void closeActual() {
        super.closeActual();

        logger.debug("Releasing slots: " + workerToSlot);
        workerToSlot.values().forEach(Slot::close);
        workerToSlot.clear();
    }
}





//protected void createWorkersForGaps(Deque<Range<Long>> gaps, long start, long end) {
//  while (!gaps.isEmpty()) {
//
//      Range<Long> gap = gaps.pollFirst();
//
//      // Check whether the gap could be added to any of the currently claimed slots
//      // We need a slot's executor in order to query the executors max request range
//
//      long coveredOffset = start;
//
//      // The covered offset is the maximum of the slots
//      for (Slot<Long> slot : workerToSlot.values()) {
//          coveredOffset = Math.max(coveredOffset, slot.getSupplier().get());
//      }
//
//      for (Entry<RangeRequestWorker<T>, Slot<Long>> e : workerToSlot.entrySet()) {
//          RangeRequestWorker<T> worker = e.getKey();
//          Slot<Long> slot = e.getValue();
//
//          long workerStartOffset = worker.getCurrentOffset();
//
//          // The worker must start before-or-at the gap
//          if (workerStartOffset > coveredOffset) {
//              continue;
//          }
//
//
//          long workerEndOffset = worker.getEndOffset();
//          long maxPossibleEndpoint = Math.min(workerEndOffset, end);
//
//
//          // If it is not possible to cover the whole gap, then put back the remaining gap for
//          // further analysis
//          Range<Long> remainingGap = Range.closedOpen(maxPossibleEndpoint, gap.upperEndpoint());
//          if (!remainingGap.isEmpty()) {
//              gaps.addFirst(remainingGap);
//          }
//
//          if (maxPossibleEndpoint > coveredOffset) {
//              coveredOffset = maxPossibleEndpoint;
//              Long oldValue = slot.getSupplier().get();
//              slot.set(maxPossibleEndpoint);
//
//              logger.debug("Updated slot " + oldValue + " -> " + slot.getSupplier().get());
//
//          }
//      }
//
//      if (coveredOffset == start) {
//          Entry<RangeRequestWorker<T>, Slot<Long>> workerAndSlot = cache.newExecutor(gap.lowerEndpoint(), gap.upperEndpoint() - gap.lowerEndpoint());
//          workerToSlot.put(workerAndSlot.getKey(), workerAndSlot.getValue());
//
//          // Put the gap back because maybe it is too large to be covered by a single executor
//          // and thus needs splitting
//          gaps.addFirst(gap);
//          // workerToSlot.entrySet().add(workerAndSlot);
//      }
//  }
//}

//public static boolean canMerge(Range<Long> a, Range<Long> b, long maxGapSize) {
//    Range<Long> gap = a.gap(b);
//    long gapSize = ContiguousSet.create(gap, DiscreteDomain.longs()).size();
//
//    boolean result = gapSize <= maxGapSize;
//    return result;
//}

//// candExecutors.addAll(cache.getExecutors());
//
//boolean optimizeGaps = true;
//Deque<Range<Long>> effectiveGaps = optimizeGaps
//      ? optimizeGaps(gaps, start, end)
//      : gaps;
//
//createWorkersForGaps(effectiveGaps, start, end);
