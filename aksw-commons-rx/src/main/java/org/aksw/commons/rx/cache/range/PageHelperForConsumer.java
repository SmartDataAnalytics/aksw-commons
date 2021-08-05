package org.aksw.commons.rx.cache.range;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.LongSupplier;

import org.aksw.commons.util.slot.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;


public class PageHelperForConsumer<T>
    extends PageHelperBase<T>
{
    private static final Logger logger = LoggerFactory.getLogger(PageHelperForConsumer.class);

    protected Map<RangeRequestWorker<T>, Slot<Long>> workerToSlot = new HashMap<>();

    protected LongSupplier offsetSupplier;
    protected SmartRangeCacheImpl<T> cache;

    public PageHelperForConsumer(SmartRangeCacheImpl<T> cache, long nextCheckpointOffset, LongSupplier offsetSupplier) {
        super(cache.newPageRange(), nextCheckpointOffset);
        this.offsetSupplier = offsetSupplier;
        this.cache = cache;
    }


    @Override
    public void checkpoint(long n) throws Exception {
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


    protected void processGaps(Deque<Range<Long>> gaps, long start, long end) {
        // candExecutors.addAll(cache.getExecutors());


        // Poor mans approach: For every gap in the look ahead range create a new worker
        // The improved approach would group 'nearby' gaps
        boolean usePoorMansApproach = true;
        if (usePoorMansApproach) {
            // while (gapIt.hasNext()) {
            while (!gaps.isEmpty()) {

                Range<Long> gap = gaps.pollFirst();

                // Check whether the gap could be added to any of the currently claimed slots
                // We need a slot's executor in order to query the executors max request range

                long coveredOffset = start;

                // The covered offset is the maximum of the slots
                for (Slot<Long> slot : workerToSlot.values()) {
                    coveredOffset = Math.max(coveredOffset, slot.getSupplier().get());
                }

                for (Entry<RangeRequestWorker<T>, Slot<Long>> e : workerToSlot.entrySet()) {
                    RangeRequestWorker<T> worker = e.getKey();
                    Slot<Long> slot = e.getValue();

                    long workerStartOffset = worker.getCurrentOffset();

                    // The worker must start before-or-at the gap
                    if (workerStartOffset > coveredOffset) {
                        continue;
                    }


                    long workerEndOffset = worker.getEndOffset();
                    long maxPossibleEndpoint = Math.min(workerEndOffset, end);


                    // If it is not possible to cover the whole gap, then put back the remaining gap for
                    // further analysis
                    Range<Long> remainingGap = Range.closedOpen(maxPossibleEndpoint, gap.upperEndpoint());
                    if (!remainingGap.isEmpty()) {
                        gaps.addFirst(remainingGap);
                    }

                    if (maxPossibleEndpoint > coveredOffset) {
                        coveredOffset = maxPossibleEndpoint;
                        Long oldValue = slot.getSupplier().get();
                        slot.set(maxPossibleEndpoint);

                        logger.debug("Updated slot " + oldValue + " -> " + slot.getSupplier().get());

                    }
                }

                if (coveredOffset == start) {
                    Entry<RangeRequestWorker<T>, Slot<Long>> workerAndSlot = cache.newExecutor(gap.lowerEndpoint(), gap.upperEndpoint() - gap.lowerEndpoint());
                    workerToSlot.put(workerAndSlot.getKey(), workerAndSlot.getValue());

                    // Put the gap back because maybe it is too large to be covered by a single executor
                    // and thus needs splitting
                    gaps.addFirst(gap);
                    // workerToSlot.entrySet().add(workerAndSlot);
                }
            }
        } else {
            throw new RuntimeException("not implemented");
//            Range<Long> gap = null;
//            while (gapIt.hasNext()) {
//                 gap = gapIt.next();
//
//                // Remove candidate executors that cannot serve the gap
//                // In the worst case no executors remain - in that case
//                // we have to create a new executor that starts after the last
//                // position that can be served by one of the current executors
//                List<RangeRequestExecutor<T>> nextCandExecutors = new ArrayList<>(candExecutors.size());
//                for (RangeRequestExecutor<T> candExecutor : candExecutors) {
//                    Range<Long> ewr = candExecutor.getWorkingRange();
//                    if (ewr.encloses(gap)) {
//                        nextCandExecutors.add(candExecutor);
//                    }
//                }
//
//                // No executor could deal with all the gaps in the read ahead range
//                // Abort the search
//                if (nextCandExecutors.isEmpty()) {
//                    break;
//                }
//            }
        }
    }

    @Override
    protected void closeActual() {
        super.closeActual();

        logger.debug("Releasing slots: " + workerToSlot);
        workerToSlot.values().forEach(Slot::close);
        workerToSlot.clear();
    }
}