package org.aksw.commons.rx.cache.range;

import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.aksw.commons.util.range.RangeBuffer;
import org.aksw.commons.util.ref.RefFuture;
import org.aksw.commons.util.sink.BulkingSink;
import org.aksw.commons.util.slot.ObservableSlottedValue;
import org.aksw.commons.util.slot.ObservableSlottedValueImpl;
import org.aksw.commons.util.slot.Slot;
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
public class RangeRequestWorker<T>
    implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger(RangeRequestWorker.class);

    /**
     * Reference to the manager - if there is a failure in processing the request the executor
     * notifies it
     */
    protected SmartRangeCacheImpl<T> cacheSystem;

    protected ObservableSlottedValue<Long, Long> endpointRequests = ObservableSlottedValueImpl.wrap(SlottedBuilderImpl.create(
            values -> values.stream().reduce(-1l,Math::max)));


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
    protected long currentPageId = -1;

    protected final long requestOffset;


    // The effective endpoint; the maximum value in contextToEndpoint
    protected volatile long effectiveEndpoint;


    /** The requestLimit must take result-set-limit on the backend into account! */
    protected long requestLimit;


    /** The number of items to process in one batch (before checking for conditions such as interrupts or no-more-demand) */
    protected int bulkSize = 16;


    // protected Map<Long, >

    // protected RangedSupplier<Long, T> backend;



    /** Report read items in chunks preferably and at most this size.
     *  Prevents synchronization on every single item. */
    protected int reportingInterval = 10;



    protected volatile long offset;


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



    public RangeRequestWorker(SmartRangeCacheImpl<T> cacheSystem, long requestOffset, long requestLimit, long terminationDelay) {
        super();
        this.cacheSystem = cacheSystem;
        this.requestOffset = requestOffset;
        this.offset = requestOffset;
        this.requestLimit = requestLimit;
        this.terminationDelay = terminationDelay;

        endpointRequests.addValueChangeListener(event -> {
            logger.info("Slot event on " + this + ": " + event);

            synchronized (endpointRequests) {
                endpointRequests.notifyAll();
            }
        });
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
        Stopwatch stopwatch = Stopwatch.createStarted();
        iterator.hasNext();
        firstItemTime = stopwatch.elapsed();

        // pauseLock.writeLock().newCondition();
        while (true) {

            if (terminationTimer.isRunning() && terminationTimer.elapsed(TimeUnit.MILLISECONDS) > terminationDelay) {
                break;
            }

            try {
                process(reportingInterval);
            } catch (Exception e) {
                 throw new RuntimeException(e);
            }


            if (iterator.hasNext()) {

                // Shut down if there is no pending request for further data
                synchronized (endpointRequests)  {
                    long maxEndpoint = endpointRequests.build();

                    if (offset >= maxEndpoint) {
                        if (waitMode) {
                            try {
                                endpointRequests.wait(terminationDelay);
                            } catch (InterruptedException e) {
                            }
                        } else {
                            // continue-mode: keep on fetching data while we still have time

                            if (maxEndpoint < 0 && !terminationTimer.isRunning()) {
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
                if (cacheSystem.knownSize > 0 && offset >= cacheSystem.knownSize) {
                    break;
                }

                if (waitMode) {
                    synchronized (endpointRequests) {
                        long maxEndpoint = endpointRequests.build();
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

        logger.debug("RangeRequestWorker is terminating");
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
        // BulkConsumer<T>

        long pageId = cacheSystem.getPageIdForOffset(offset);
        int offsetInPage = cacheSystem.getIndexInPageForOffset(offset);

        // Make sure we don't acquire a page while close is invoked
        // FIXME Only acquire a page if it is necessary

        if (currentPageId != pageId) {
            synchronized (this) {
                if (currentPageRef != null) {
                    currentPageRef.close();
                }

                currentPageId = pageId;
                currentPageRef = cacheSystem.getPageForPageId(pageId);
            }
        }

        RangeBuffer<T> rangeBuffer = currentPageRef.await();

        BulkingSink<T> sink = new BulkingSink<>(bulkSize,
                (arr, start, len) -> rangeBuffer.putAll(offsetInPage, arr, start, len));


        long maxEndpoint = endpointRequests.build();

        int numItemsUntilPageEnd = rangeBuffer.getCapacity() - offsetInPage;
        int numItemsUntilPageKnownSize = rangeBuffer.getKnownSize() >= 0 ? rangeBuffer.getKnownSize() : rangeBuffer.getCapacity();
        long numItemsUtilRequestLimit = (requestOffset + requestLimit) - offset;
        long numItemsUntilEndpoint = maxEndpoint - offset;

        long limit = Math.min(Math.min(Math.min(
                reportingInterval,
                numItemsUntilPageEnd),
                numItemsUntilPageKnownSize),
                numItemsUtilRequestLimit);

        // In wait mode stop exactly at maxEndpoint
        if (waitMode) {
            limit = Math.min(limit, numItemsUntilEndpoint);
        }

        // Explicitly acquire the write lock in order to update the
        // buffer and possibly the known size in one operation
        Lock writeLock = rangeBuffer.getReadWriteLock().writeLock();
        writeLock.lock();
        try {

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

            // TODO set the known size if the page is full
            if (!hasNext && numItemsProcessed < requestLimit) {
                if (rangeBuffer.getKnownSize() < 0) {
                    int knownPageSize = offsetInPage + i;
                    rangeBuffer.setKnownSize(knownPageSize);
                    cacheSystem.setKnownSize(offset);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }


    public Slot<Long> getEndpointSlot() {
        return endpointRequests.newSlot();
    }
}

