package org.aksw.commons.io.cache;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.LongUnaryOperator;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.input.SequentialReader;
import org.aksw.commons.io.slice.SliceAccessor;
import org.aksw.commons.io.slice.Slice;
import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;
import org.aksw.commons.util.lock.LockUtils;
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
import com.google.common.primitives.Ints;

/**
 * A producer task: Takes items from an iterator and writes them them to pages
 *
 * @author raven
 *
 * @param <A>
 */
public class RangeRequestWorkerImpl<A>
    extends AutoCloseableWithLeakDetectionBase
    implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger(RangeRequestWorkerImpl.class);



    /**
     * Reference to the manager - if there is a failure in processing the request the executor
     * notifies it
     */
    protected AdvancedRangeCacheImpl<A> cacheSystem;


    protected ArrayOps<A> arrayOps;

    /**
     * Demands from clients to load data up to the supplied value.
     * A client can unregister a demand any time.
     *
     */
    protected ObservableSlottedValue<Long, Long> endpointDemands = ObservableSlottedValueImpl.wrap(SlottedBuilderImpl.create(
            values -> values.stream().reduce(-1l,Math::max)));


    /**
     * The data supplying iterator.
     * If all data in the range of operation of this worker is cached then no backend request is made.
     */
    // protected Iterator<T> iterator = null;

    protected SequentialReader<A> sequentialReader;

    /** The disposable of the data supplier */
    // protected Disposable disposable;

    /** The data slice */
    protected Slice<A> slice;

    /** The pages claimed by this worker; obtained from slice */
    protected SliceAccessor<A> pageRange;

    protected long currentPageId = -1;

    /** The offset of the original request */
    protected final long requestOffset;

    /** The requestLimit must take result-set-limit on the backend into account! */
    protected final long requestLimit;


    /** Metadata supplier to support scheduling: For a given offset return the maximum length of a run of items that have already been retrieved
     *  before a separate request should be fired instead of retrieving those items twice and wasting data volume */
    protected LongUnaryOperator offsetToMaxAllowedRefetchCount;


    /** The number of items to process in one batch (before checking for conditions such as interrupts or no-more-demand) */
    protected int bulkSize;

    /** Report read items in chunks preferably and at most this size.
     *  Prevents having to synchronize on every single item. */
    // protected int reportingInterval = bulkSize;

    /** The current offset*/
    protected volatile long offset;

    /** The offset at which the next checkpoint is sheduled */
    protected long nextCheckpointOffset;

    /**
     * Task termination may be delayed in millis in order to allow it to recover should another observer register
     * in the delay phase
     */
    protected Duration terminationDelay;

    /**
     * Wait mode - true: do not fetch more data then there is demand - false: keep pre-fetching data
     *
     */
    protected IdleMode idleMode = IdleMode.READ_AHEAD;

    /**
     * A timer is started as soon as there is no more explicit demand for data.
     * Depending on the idle mode the worker can either pause or read ahead until the termination delay is reached.
     */
    protected transient Stopwatch terminationTimer = Stopwatch.createUnstarted();


    /*
     * Statistics
     */
    /** Time it took to retrieve the first item */
    protected Duration firstItemTime = null;

    /** Throughput in items / second */
    protected long numItemsProcessed = 0;

    /** Total time */
    protected long processingTimeInNanos = 0;



    public RangeRequestWorkerImpl(
            AdvancedRangeCacheImpl<A> cacheSystem,
            long requestOffset,
            long requestLimit,
            int bulkSize,
            Duration terminationDelay) {
        super();
        this.cacheSystem = cacheSystem;
        this.arrayOps = cacheSystem.getSlice().getArrayOps();
        this.requestOffset = requestOffset;
        this.offset = requestOffset;
        this.requestLimit = requestLimit;
        this.bulkSize = bulkSize;
        this.terminationDelay = terminationDelay;

        this.slice = cacheSystem.getSlice();
        this.pageRange = slice.newSliceAccessor();

        // TODO Make configurable
        this.offsetToMaxAllowedRefetchCount = offset -> 5000;

        // Ensure to trigger a checkpoint as first action of a run
        this.nextCheckpointOffset = offset;

        endpointDemands.addValueChangeListener(event -> {
            if (logger.isTraceEnabled()) {
                logger.trace("End-offset of data range demand updated to " + this + ": " + event);
            }

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
        if (sequentialReader != null) {
            try {
                sequentialReader.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // Free claimed resources (pages)
        // pageRange.releaseAll();
        pageRange.close();
    }


    protected synchronized void initBackendRequest() {
        // Synchronize because abort may be called concurrently
        if (!isClosed) {
            // Flowable<A> backendFlow = cacheSystem.getBackend().apply(Range.atLeast(offset));
            // iterator = backendFlow.blockingIterable().iterator();
            // disposable = (Disposable)iterator;

            // TODO Init the reader
            sequentialReader = cacheSystem.getDataSource().newInputStream(Range.atLeast(requestOffset));

        } else {
            return; // Exit immediately due to abort
        }
    }

    @Override
    public void run() {
//        if (offset == 4516) {
//            System.out.println("debug point");
//        }

        try {
            checkpoint();
            // If the checkpoint offset was not advanced then we reached end of data
            if (offset != nextCheckpointOffset) {
                runCore();
            }

//            if (offset == 62997688) {
//                System.out.println("debug point");
//            }
            logger.debug("RangeRequestWorker normal termination at offset " + offset);
        } catch (Exception e) {
            logger.error("RangeRequestWorker exceptional termination at offset " + offset, e);
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
        long remainingAllowedItems = requestLimit - numItemsProcessed;
        long maxAllowedRefetchCount = getMaxAllowedRefetchCount(offset);
        long effectiveLimit = Math.min(maxAllowedRefetchCount, remainingAllowedItems);
        long claimAheadRangeEnd = offset + effectiveLimit;

        Range<Long> claimAheadRange = Range.closedOpen(offset, claimAheadRangeEnd);

        // slice.computeFromMetaData(false, metaData -> {
        LockUtils.runWithLock(slice.getReadWriteLock().readLock(), () -> {
            RangeSet<Long> gaps = slice.getGaps(claimAheadRange);
            if (gaps.isEmpty()) {
                nextCheckpointOffset = claimAheadRangeEnd;
            } else {
                Range<Long> lastGap = gaps.asDescendingSetOfRanges().iterator().next();
                long last = LongMath.saturatedAdd(ContiguousSet.create(lastGap, DiscreteDomain.longs()).last(), 1);

                nextCheckpointOffset = last;
            }
            // return null;
        });
        //});
    }


//    public void run2() {
//
//    	synchronized (cacheSystem.getExecutorCreationReadLock()) {
//
//    		// Check if there are any more demands -if not then we can safely shut dowwn
//
//
//		}
//
//    }


    /**
     *
     * A worker can only shutdown if it holds the workerSetLock: A concurrent RangeRequestIterator may schedule a new task for a worker
     * that was just about to terminate.
     *
     * Shutdown conditions:
     * - idle for too long
     * - reached end of data
     * -
     * - exception
     * @throws Exception
     *
     */
    public void runCore() throws Exception {

        A buffer = arrayOps.create(bulkSize);

        initBackendRequest();

        // Measuring the time for the first item may be meaningless if an underlying cache is used
        // It may be better to measure on e.g. the HTTP level using interceptors on HTTP client

        boolean isFirstRun = true;
        Stopwatch stopwatch = Stopwatch.createStarted();
        process(buffer, 0, 1);

        firstItemTime = stopwatch.elapsed();

        // pauseLock.writeLock().newCondition();
        while (true) {

            if (terminationTimer.isRunning() && terminationTimer.elapsed(TimeUnit.MILLISECONDS) > terminationDelay.toMillis()) {
                break;
            }

            if (offset == nextCheckpointOffset) {
                checkpoint();

                // No progress after checkpoint - we have reached the end of data
                if (offset == nextCheckpointOffset) {
                    break;
                }
            }

            boolean hasNext;
            try {
                // Align with bulk size; this should give more aesthetic numbers in debugging and logging
                if (isFirstRun) {
                    isFirstRun = false;
                    hasNext = process(buffer, 1, bulkSize - 1) >= 0;
                } else {
                    hasNext = process(buffer, 0, bulkSize) >= 0;
                }

            } catch (Exception e) {
                 throw new RuntimeException(e);
            }


            if (hasNext) {

                // Shut down if there is no pending request for further data
                synchronized (endpointDemands)  {
                    // Endpoint is exclusive (offset must be strictly lower)
                    long maxDemandedEndpoint = endpointDemands.build();

                    if (offset >= maxDemandedEndpoint) {
                        switch (idleMode) {
                        case PAUSE:
                            try {
                                endpointDemands.wait(terminationDelay.toMillis());
                            } catch (InterruptedException e) {
                            }
                            break;
                        case READ_AHEAD:
                            // continue-mode: keep on fetching data while we still have time

                            if (maxDemandedEndpoint < 0 && !terminationTimer.isRunning()) {
                                logger.debug("No more demand for data - starting termination timer");
                                terminationTimer.start();
                            }
                            break;
                        }
                    } else {
                        // There is demand for data - Reset the termination timer
                        if (terminationTimer.isRunning()) {
                            logger.debug("New demand for data - resetting termination timer");
                            terminationTimer.reset();
                        }

                    }
                }

                // If there are no more slots or we have reached to known size then terminate
                Lock readLock = slice.getReadWriteLock().readLock();
                readLock.lock();
                try {
                    long knownSize = slice.getKnownSize();
                    if (knownSize >= 0 && offset >= knownSize) {
                        break;
                    }
                } finally {
                    readLock.unlock();
                }

                if (IdleMode.PAUSE.equals(idleMode)) {
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


    /** The offset of the first item that won't be served by this worker */
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
    public int process(A buffer, int bufferOffset, int n) throws Exception {
        if (n <= 0) {
            throw new IllegalArgumentException("Request to process 0 or fewer items is invalid");
        }

        pageRange.claimByOffsetRange(offset, offset + n);

        long numItemsUntilNextCheckpoint = nextCheckpointOffset - offset;

        long remainingReads = Math.min(n,
                Math.min(bulkSize, numItemsUntilNextCheckpoint));

        // In wait mode stop exactly at maxEndpoint (do not read ahead)
        if (IdleMode.PAUSE.equals(idleMode)) {
            long maxEndpointDemand = endpointDemands.build();
            long numItemsUntilEndpoint = maxEndpointDemand - offset;
            remainingReads = Math.min(remainingReads, numItemsUntilEndpoint);
        }

        // Explicitly acquire the write lock in order to update the
        // buffer and possibly the known size in one operation
        // pageRange.lock();

        Lock writeLock = slice.getReadWriteLock().writeLock();
        writeLock.lock();

        // long itemsProcessedNow = 0;

        int remainingReadsInt = Ints.checkedCast(remainingReads);
        int numItemsOfLastRead = 0;
        int result = 0;
        try {
            // Note: Read should never return 0!
            while (numItemsOfLastRead >= 0 && remainingReadsInt != 0 &&
                    !isClosed && !Thread.interrupted() &&
                    (numItemsOfLastRead = sequentialReader.read(buffer, bufferOffset, remainingReadsInt)) >= 0) {
                pageRange.write(offset, buffer, bufferOffset, numItemsOfLastRead);

                remainingReadsInt -= numItemsOfLastRead;
                result += numItemsOfLastRead;
                bufferOffset += numItemsOfLastRead;
                offset += numItemsOfLastRead;
                // System.out.println("write at offset " + offset);
                // itemsProcessedNow += numItemsReadTmp;
            }
            // offset += result;
            numItemsProcessed += result;

            // result becomes -1 if it is 0 for a non-zero length
            result = result == 0 && n != 0 ? -1 : result;

            // int numItemsRead = numItemsProcessed;

//        	LockUtils.runWithLock(slice.getReadWriteLock().writeLock(), () -> {

                slice.updateMinimumKnownSize(offset);

                // If there is no further item although the request range has not been covered
                // then we have detected the end

                // Note: We may have also just hit the backend's result-set-limit
                // This is the case if there is
                // (1) a known result-set-limit value on the smart cache
                // (2) a known higher offset in the smart cache or
                // (3) another request with the same offset that yield results

                // TODO set the known size if the page is full
                if (numItemsOfLastRead < 0 && numItemsProcessed < requestLimit) {
                    if (slice.getKnownSize() < 0) {
                        // long knownPageSize = offsetInPage + i;
                        // rangeBuffer.setKnownSize(knownPageSize);
                        slice.setMaximumKnownSize(offset);
                    }
                }

                if (logger.isTraceEnabled()) {
//                    if (offset == 63008072) {
//                        System.out.println("debug point");
//                    }

                    logger.trace(String.format("Signalling data condition to clients - offset: %1$d, processed: %2$d, limit:  %3$d, loaded ranges: %4$s", offset, numItemsProcessed, requestLimit, slice.getLoadedRanges()));
                }

                slice.getHasDataCondition().signalAll();

        } finally {
            // pageRange.unlock();
            writeLock.unlock();
        }

        return result;
    }



    /**
     * Acquire a new slot into which the end-offset of a demanded data range can be put.
     *
     * FIXME The worker may just have terminated; so we need synchronization with the workerSyncLock such that during scheduling workers don't
     * disappear.
     *
     * @return
     */
    public Slot<Long> newDemandSlot() {
        synchronized (endpointDemands) {
            return endpointDemands.newSlot();
        }
    }
}



//});

//int i = 0;
//boolean hasNext;
//while ((hasNext = iterator.hasNext()) && i < limit && !isClosed && !Thread.interrupted()) {
//  A item = iterator.next();
//  ++i;
//  sink.accept(item);
//}

//sink.flush();
//sink.close();

//slice.mutateMetaData(metaData -> {
//  metaData.setMinimumKnownSize(offset);
//
//  // If there is no further item although the request range has not been covered
//  // then we have detected the end
//
//  // Note: We may have also just hit the backend's result-set-limit
//  // This is the case if there is
//  // (1) a known result-set-limit value on the smart cache
//  // (2) a known higher offset in the smart cache or
//  // (3) another request with the same offset that yield results
//
//  // TODO set the known size if the page is full
//  if (!hn && numItemsProcessed < requestLimit) {
//      if (metaData.getKnownSize() < 0) {
//          // long knownPageSize = offsetInPage + i;
//          // rangeBuffer.setKnownSize(knownPageSize);
//          metaData.setKnownSize(offset);
//      }
//  }
//  logger.info("Signalling data condition to clients - " + hn + " - " + numItemsProcessed + " " + requestLimit);
//});

