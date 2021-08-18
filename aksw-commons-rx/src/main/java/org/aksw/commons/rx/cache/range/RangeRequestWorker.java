package org.aksw.commons.rx.cache.range;

import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.LongUnaryOperator;

import org.aksw.commons.util.ref.RefFuture;
import org.aksw.commons.util.sink.BulkingSink;
import org.aksw.commons.util.slot.ObservableSlottedValue;
import org.aksw.commons.util.slot.ObservableSlottedValueImpl;
import org.aksw.commons.util.slot.Slot;
import org.aksw.commons.util.slot.SlottedBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.math.LongMath;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * A producer task: Takes items from an iterator and writes them them to pages
 *
 * @author raven
 *
 * @param <T>
 */
public class RangeRequestWorker<T>
    extends CloseHelper
    implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger(RangeRequestWorker.class);

    /**
     * Reference to the manager - if there is a failure in processing the request the executor
     * notifies it
     */
    protected SmartRangeCacheImpl<T> cacheSystem;

    /** Demands from clients to load data up to the supplied value */
    protected ObservableSlottedValue<Long, Long> endpointDemands = ObservableSlottedValueImpl.wrap(SlottedBuilderImpl.create(
            values -> values.stream().reduce(-1l,Math::max)));


    /**
     * The data supplying iterator.
     * If all data in the range of operation of this worker is cached then no backend request is made.
     */
    protected Iterator<T> iterator = null;

    /** The disposable of the data supplier */
    protected Disposable disposable;

    /** The data slice */
    protected Slice<T> slice;

    /** The pages claimed by this worker; obtained from slice */
    protected PageRange<T> pageRange;

    protected long currentPageId = -1;

    /** The offset of the original request */
    protected final long requestOffset;

    /** The requestLimit must take result-set-limit on the backend into account! */
    protected final long requestLimit;


    /** The effective endpoint; the maximum value in contextToEndpoint */
    // protected volatile long effectiveEndpoint;


    /** For a given offset return the maximum length of a run of items that have already been retrieved
     *  before a separate request should be fired instead of retrieving those items twice and wasting data volume */
    protected LongUnaryOperator offsetToMaxAllowedRefetchCount;


    // protected Map<Long, >

    // protected RangedSupplier<Long, T> backend;



    /** The number of items to process in one batch (before checking for conditions such as interrupts or no-more-demand) */
    protected int bulkSize = 16;

    /** Report read items in chunks preferably and at most this size.
     *  Prevents synchronization on every single item. */
    protected int reportingInterval = bulkSize;



    protected volatile long offset;

    protected long nextCheckpointOffset;

    /**
     * Task termination may be delayed in order to allow it to recover should another observer register
     * in the delay phase
     */
    protected long terminationDelay;

    /**
     * Wait mode - true: do not fetch more data then there is demand - false: keep pre-fetching data
     *
     */
    protected boolean waitMode = false;

    /**
     * A timer is started as soon as there is no more explicit demand for data
     * Until that timer reaches terminationDelay, the worker can keep fetching data from the backend
     */
    protected transient Stopwatch terminationTimer = Stopwatch.createUnstarted();




    /*
     * Statistics
     */
    /** Time it took to retrieve the first item */
    protected Duration firstItemTime = null;

    /** Throughput in items / second */
    protected long numItemsProcessed = 0;
    protected long processingTimeInNanos = 0;



    public RangeRequestWorker(
            SmartRangeCacheImpl<T> cacheSystem,
            long requestOffset,
            long requestLimit,
            long terminationDelay) {
        super();
        this.cacheSystem = cacheSystem;
        this.requestOffset = requestOffset;
        this.offset = requestOffset;
        this.requestLimit = requestLimit;
        this.terminationDelay = terminationDelay;

        this.slice = cacheSystem.getSlice();
        this.pageRange = slice.newPageRange();

        this.offsetToMaxAllowedRefetchCount = offset -> 5000;

        endpointDemands.addValueChangeListener(event -> {
            logger.info("Slot event on " + this + ": " + event);

            synchronized (endpointDemands) {
                endpointDemands.notifyAll();
            }
        });
    }

    public long getMaxAllowedRefetchCount(long offset) {
        return offsetToMaxAllowedRefetchCount.applyAsLong(offset);
    }

    /** Time in seconds it took to obtain the first item */
    public Duration getFirstItemTime() {
        return firstItemTime;
    }

    /** Throughput measured in items per second */
    public float getThroughput() {
        return numItemsProcessed / (float)(processingTimeInNanos / 1e9);
    }



    /**
     * Estimated time of arrival at the given index in seconds
     * Index must be greater or equal to offset
     *
     * Call {@link #pause()} before calling the method.
     */
    public float etaAtIndex(long index) {
        long distance = index - offset;

        float throughput = getThroughput();
        float result = distance * throughput;
        return result;
    }


    /**
     * Stops processing of this executor and
     * also disposes the underlying data supplier (which is expected to terminate)
     */
    @Override
    protected void closeActual() {
        // Cancel the backend process
        if (disposable != null) {
            disposable.dispose();
        }

        // Free claimed resources (pages)
        pageRange.releaseAll();
    }


    protected synchronized void initBackendRequest() {
        // Synchronize because abort may be called concurrently
        if (!isClosed) {
            Flowable<T> backendFlow = cacheSystem.getBackend().apply(Range.atLeast(offset));
            iterator = backendFlow.blockingIterable().iterator();
            disposable = (Disposable)iterator;
        } else {
            return; // Exit immediately due to abort
        }
    }

    @Override
    public void run() {
        try {
            checkpoint();
            // If the checkpoint offset was not advanced then we reached end of data
            if (nextCheckpointOffset != offset) {
                runCore();
            }
            logger.debug("RangeRequestWorker normal termination");
        } catch (Exception e) {
            logger.error("RangeRequestWorker exceptional termination", e);
            throw new RuntimeException(e);
        } finally {
            close();
        }
    }


    protected void checkpoint() {
        // TODO We may have maxAllowedRefetchCount items ahead
        // but a demand that spans across this range
        // Hence, a new backend request needs to be made

        // A demand overrides the worker's maxAllowedRefetchCount
        // Demands should take refetching of ranges into account

        long maxAllowedRefetchCount = getMaxAllowedRefetchCount(offset);
        long effectiveLimit = Math.min(maxAllowedRefetchCount, requestLimit);

        Range<Long> claimAheadRange = Range.closedOpen(offset, offset + effectiveLimit);

        try (RefFuture<SliceMetaData> ref = slice.getMetaData()) {
            SliceMetaData metaData = ref.await();

            RangeSet<Long> gaps = metaData.getGaps(claimAheadRange);
            if (gaps.isEmpty()) {
                // Nothing todo; not updating the limits will cause the worker to terminate
            } else {
                Range<Long> lastGap = gaps.asDescendingSetOfRanges().iterator().next();
                long last = LongMath.saturatedAdd(ContiguousSet.create(lastGap, DiscreteDomain.longs()).last(), 1);

                nextCheckpointOffset = last;
            }
        }

//
//
//        pageRange.claimByOffsetRange(offset, offset + effectiveLimit);
//        pageRange.lock();
//
//        try {
//            // Deque<Range<Long>> gaps = pageRange.getGaps();
//            slice.getMetaData().await().getGaps(getWorkingRange());
//
//            if (gaps.isEmpty()) {
//                // Nothing todo; not updating the limits will cause the worker to terminate
//            } else {
//                Range<Long> lastGap = gaps.getLast();
//                long last = LongMath.saturatedAdd(ContiguousSet.create(lastGap, DiscreteDomain.longs()).last(), 1);
//
//                nextCheckpointOffset = last;
//            }
//
//        } finally {
//            pageRange.unlock();
//        }

    }

    public void runCore() {
        initBackendRequest();

        // Measuring the time for the first item may be meaningless if an underlying cache is used
        // It may be better to measure on e.g. the HTTP level using interceptors on HTTP client
        Stopwatch stopwatch = Stopwatch.createStarted();
        iterator.hasNext();
        firstItemTime = stopwatch.elapsed();

        // pauseLock.writeLock().newCondition();
        while (true) {

            if (terminationTimer.isRunning() && terminationTimer.elapsed(TimeUnit.MILLISECONDS) > terminationDelay) {
                break;
            }

            if (offset == nextCheckpointOffset) {
                checkpoint();

                if (offset == nextCheckpointOffset) {
                    break;
                }
            }

            try {
                process(reportingInterval);
            } catch (Exception e) {
                 throw new RuntimeException(e);
            }


            if (iterator.hasNext()) {

                // Shut down if there is no pending request for further data
                synchronized (endpointDemands)  {
                    long maxDemandedEndpoint = endpointDemands.build();

                    if (offset >= maxDemandedEndpoint) {
                        if (waitMode) {
                            try {
                                endpointDemands.wait(terminationDelay);
                            } catch (InterruptedException e) {
                            }
                        } else {
                            // continue-mode: keep on fetching data while we still have time

                            if (maxDemandedEndpoint < 0 && !terminationTimer.isRunning()) {
                                logger.debug("No more demand for data - starting termination timer");
                                terminationTimer.start();
                            }
                        }
                    } else {
                        // There is demand for data - Reset the termination timer
                        if (terminationTimer.isRunning()) {
                            logger.debug("New demand for data - stopping/resetting termination timer");
                            terminationTimer.reset();
                        }

                    }
                }

                // If there are no more slots or we have reached to known size then terminate
                try (RefFuture<SliceMetaData> ref = slice.getMetaData()) {
                    SliceMetaData metaData = ref.await();

                    long knownSize = metaData.getKnownSize();
                    if (knownSize >= 0 && offset >= knownSize) {
                        break;
                    }
                }

                if (waitMode) {
                    synchronized (endpointDemands) {
                        long maxEndpoint = endpointDemands.build();
                        if (maxEndpoint < 0) {
                            break;
                        }
    //                    if (offset >= maxEndpoint) {
    //                        break;
    //                    }
                    }
                }

            } else {
                // If there is no more data then terminate immediately
                break;
            }
        }
    }


    public long getCurrentOffset() {
        return offset;
    }


    public long getEndOffset() {
        return 	LongMath.saturatedAdd(requestOffset, requestLimit);
    }

    public Range<Long> getWorkingRange() {
        return Range.closedOpen(offset, getEndOffset());
    }


    /**
     * Process up to n more items.
     *
     * @throws Exception
     *
     */
    public void process(int n) throws Exception {

//        long pageId = cacheSystem.getPageIdForOffset(offset);
//        int offsetInPage = cacheSystem.getIndexInPageForOffset(offset);
//
//        RefFuture<RangeBuffer<T>> currentPageRef = pageRange.getClaimedPages().get(pageId);
//
//        RangeBuffer<T> rangeBuffer = currentPageRef.await();

        pageRange.claimByOffsetRange(offset, offset + n);

        BulkingSink<T> sink = new BulkingSink<>(bulkSize,
                (arr, start, len) -> pageRange.putAll(offset, arr, start, len));


//        int numItemsUntilPageEnd = rangeBuffer.getCapacity() - offsetInPage;
//        int numItemsUntilPageKnownSize = rangeBuffer.getKnownSize() >= 0 ? rangeBuffer.getKnownSize() : rangeBuffer.getCapacity();
        // long numItemsUtilRequestLimit = (requestOffset + requestLimit) - offset;
        long numItemsUntilNextCheckpoint = nextCheckpointOffset - offset;

        long limit = Math.min(
                reportingInterval,
                numItemsUntilNextCheckpoint);

//        if (limit < reportingInterval) {
//            System.out.println("DEBUG POINT");
//        }

        // In wait mode stop exactly at maxEndpoint
        if (waitMode) {
            long maxEndpointDemand = endpointDemands.build();
            long numItemsUntilEndpoint = maxEndpointDemand - offset;
            limit = Math.min(limit, numItemsUntilEndpoint);
        }

        // Explicitly acquire the write lock in order to update the
        // buffer and possibly the known size in one operation
//        Lock writeLock = rangeBuffer.getReadWriteLock().writeLock();
//        writeLock.lock();
        pageRange.lock();
        try {

            int i = 0;
            boolean hasNext;
            while ((hasNext = iterator.hasNext()) && i < limit && !isClosed && !Thread.interrupted()) {
                T item = iterator.next();
                ++i;
                sink.accept(item);
            }

            sink.flush();
            sink.close();

            numItemsProcessed += i;
            offset += i;


            try (RefFuture<SliceMetaData> ref = slice.getMetaData()) {
                SliceMetaData metaData = ref.await();
                Lock writeLock = metaData.getReadWriteLock().writeLock();
                writeLock.lock();

                try {
                    metaData.setMinimumKnownSize(offset);


                    // If there is no further item although the request range has not been covered
                    // then we have detected the end

                    // Note: We may have also just hit the backend's result-set-limit
                    // This is the case if there is
                    // (1) a known result-set-limit value on the smart cache
                    // (2) a known higher offset in the smart cache or
                    // (3) another request with the same offset that yield results

                    // TODO set the known size if the page is full
                    if (!hasNext && numItemsProcessed < requestLimit) {
                        if (metaData.getKnownSize() < 0) {
                            // long knownPageSize = offsetInPage + i;
                            // rangeBuffer.setKnownSize(knownPageSize);
                            metaData.setKnownSize(offset);
                        }
                    }
                    metaData.getHasDataCondition().signalAll();
                } finally {
                    writeLock.unlock();
                }
            }
        } finally {
            //writeLock.unlock();
            pageRange.unlock();
        }
    }


    public Slot<Long> getEndpointSlot() {
        return endpointDemands.newSlot();
    }
}

//
//this.pageHelper = new PageHelperBase<>(cacheSystem, requestOffset) {
//  protected void processGaps(Deque<Range<Long>> gaps, long start, long end) {
//      // Check if there are any gaps within the max refetch range
//      long maxAllowedRefetchCount = getMaxAllowedRefetchCount(start);
//
//      // Range<Long> refetchRange = Range.closedOpen(start, end);
//
//      // If there is no gap in range then cancel the backend request
//      // if there is data demand then create a new executor
//      if (gaps.isEmpty()) {
//
//      } else {
//
//      }
//
//
//
//
//      // TODO Update the processing range
//
//
//  }
//};
//
