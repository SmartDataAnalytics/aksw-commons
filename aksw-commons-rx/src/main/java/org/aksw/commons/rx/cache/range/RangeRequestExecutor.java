package org.aksw.commons.rx.cache.range;

import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.aksw.commons.util.range.RangeBuffer;
import org.aksw.commons.util.ref.RefFuture;
import org.aksw.commons.util.sink.BulkingSink;
import org.aksw.commons.util.slot.Slot;
import org.aksw.commons.util.slot.SlottedBuilder;
import org.aksw.commons.util.slot.SlottedBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;
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
public class RangeRequestExecutor<T>
    implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger(RangeRequestExecutor.class);

    /**
     * Reference to the manager - if there is a failure in processing the request the executor
     * notifies it
     */
    protected SmartRangeCacheImpl<T> cacheSystem;

    protected SlottedBuilder<Long, Long> endpointRequests = SlottedBuilderImpl.create(
            values -> values.stream().reduce(-1l,Math::max));


    /** The data supplying iterator */
    protected Iterator<T> iterator;

    /** The disposable of the data supplier */
    protected Disposable disposable;

    /** Whether processing is aborted */
    protected boolean isAborted = false;


    /** The pages claimed by the executor */
    // protected Set<Ref<Page<T>>> claimedPages;

    /**
     * The page the executor is currently writing to
     * Preloading of pages is requested by the client iterator
     * So we probably there is not much benefit from doing it here too
     */
    protected RefFuture<RangeBuffer<T>> currentPageRef;

    protected long requestOffset;


    // The effective endpoint; the maximum value in contextToEndpoint
    protected long effectiveEndpoint;


    /** The requestLimit must take result-set-limit on the backend into account! */
    protected long requestLimit;


    // protected Map<Long, >

    // protected RangedSupplier<Long, T> backend;



    /** Report read items in chunks preferably and at most this size.
     *  Prevents synchronization on every single item. */
    protected int reportingInterval = 10;



    protected long offset;


    /**
     * Task termination may be delayed in order to allow it to recover should another observer register
     * in the delay phase
     */
    protected long terminationDelay;





    /*
     * Statistics
     */
    /** Time it took to retrieve the first item */
    protected Duration firstItemTime = null;

    /** Throughput in items / second */
    protected long numItemsProcessed = 0;
    protected long processingTimeInNanos = 0;


    protected ReentrantReadWriteLock executorCreationLock = new ReentrantReadWriteLock();


    public RangeRequestExecutor(SmartRangeCacheImpl<T> cacheSystem, long requestOffset, long requestLimit, long terminationDelay) {
        super();
        this.cacheSystem = cacheSystem;
        this.requestOffset = requestOffset;
        this.offset = requestOffset;
        this.requestLimit = requestLimit;
        this.terminationDelay = terminationDelay;
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
     * also disposes the undelying data supplier (which is expected to terminate)
     */
    public void abort() {
        synchronized (this) {
            if (!isAborted && disposable != null) {
                disposable.dispose();
                isAborted = true;
            }
        }
    }


    public void close() {
        // Free claimed resources
        synchronized (this) {
            if (currentPageRef != null) {
                currentPageRef.close();
            }
        }
    }


    protected void init() {
        // Synchronize because abort may be called concurrently
        synchronized (this) {
            if (!isAborted) {
                Flowable<T> backendFlow = cacheSystem.getBackend().apply(Range.atLeast(offset));
                iterator = backendFlow.blockingIterable().iterator();
                disposable = (Disposable)iterator;
            } else {
                return; // Exit immediately due to abort
            }
        }
    }

    @Override
    public void run() {
        try {
            runCore();
        } catch (Exception e) {
            logger.error("Exceptional termination", e);
            e.printStackTrace();
        }
    }

    public void runCore() {
        init();

        // Measuring the time for the first item may be meaningless if an underlying cache is used
        // It may be better to measure on e.g. the HTTP level using interceptors on HTTP client
        Stopwatch firstItemTimer = Stopwatch.createStarted();
        iterator.hasNext();
        Duration firstItemTime = firstItemTimer.elapsed();

        // pauseLock.writeLock().newCondition();
        while (true) {
            try {
                process(reportingInterval);
            } catch (Exception e) {
                 throw new RuntimeException(e);
            }


            if (iterator.hasNext()) {

                // Shut down if there is no pending request for further data
                long maxEndpoint = endpointRequests.build();
                if (offset >= maxEndpoint) {
                    try {
                        Thread.sleep(terminationDelay);
                    } catch (InterruptedException e) {
                    }
                }

                maxEndpoint = endpointRequests.build();
                if (offset >= maxEndpoint) {
                    break;
                }

            } else {
                break;
            }
        }

        close();
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
     * @throws ExecutionException
     * @throws InterruptedException
     *
     */
    public void process(int n) throws InterruptedException, ExecutionException {
        int bulkSize = 16;
        // BulkConsumer<T>

        long pageId = cacheSystem.getPageIdForOffset(offset);
        int offsetInPage = cacheSystem.getIndexInPageForOffset(offset);

        // Make sure we don't acquire a page while close is invoked
        // FIXME Only acquire a page if it is necessary
        synchronized (this) {
            if (currentPageRef != null) {
                currentPageRef.close();
            }

            currentPageRef = cacheSystem.getPageForPageId(pageId);
        }


        RangeBuffer<T> rangeBuffer = currentPageRef.await();

        BulkingSink<T> sink = new BulkingSink<>(bulkSize,
                (arr, start, len) -> rangeBuffer.putAll(offsetInPage, arr, start, len));


        long maxEndpoint = endpointRequests.build();

        int numItemsUntilPageEnd = rangeBuffer.getCapacity() - offsetInPage;
        long numItemsUtilRequestLimit = (requestOffset + requestLimit) - offset;
        long numItemsUntilEndpoint = maxEndpoint - offset;

        long limit = Math.min(Math.min(Math.min(
                reportingInterval,
                numItemsUntilPageEnd),
                numItemsUtilRequestLimit),
                numItemsUntilEndpoint);

        int i = 0;
        boolean hasNext;
        while ((hasNext = iterator.hasNext()) && i < limit && !isAborted && !Thread.interrupted()) {
            T item = iterator.next();
            ++i;
            sink.accept(item);
        }
        sink.flush();
        sink.close();

        numItemsProcessed += i;
        offset += i;

        // If there is no further item although the request range has not been covered
        // then we have detected the end

        // Note: We may have also just hit the backend's result-set-limit
        // This is the case if there is
        // (1) a known result-set-limit value on the smart cache
        // (2) a known higher offset in the smart cache or
        // (3) another request with the same offset that yield results
        if (!hasNext && numItemsProcessed < requestLimit) {
            cacheSystem.setKnownSize(offset);
        }
    }


    public Slot<Long> getEndpointSlot() {
        return endpointRequests.newSlot();
    }
}

