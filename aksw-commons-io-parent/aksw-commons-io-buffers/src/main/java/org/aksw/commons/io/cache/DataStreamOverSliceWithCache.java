package org.aksw.commons.io.cache;

import java.io.IOException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.LongSupplier;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.input.DataStream;
import org.aksw.commons.io.slice.SliceAccessor;
import org.aksw.commons.io.slice.Slice;
import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;
import org.aksw.commons.util.lock.LockUtils;
import org.aksw.commons.util.range.RangeUtils;
import org.aksw.commons.util.slot.Slot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;
import com.google.common.math.LongMath;
import com.google.common.primitives.Ints;


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
public class DataStreamOverSliceWithCache<A>
    extends AutoCloseableWithLeakDetectionBase
    implements DataStream<A>
{
    private static final Logger logger = LoggerFactory.getLogger(DataStreamOverSliceWithCache.class);

    protected Slice<A> slice;
    protected AdvancedRangeCacheImpl<A> cache;
    protected SliceAccessor<A> pageRange;

    /**
     * The original request range by this request.
     * In general, the original request range has to be broken down into smaller ranges
     * because of result size limits of the backend
     */
    protected Range<Long> requestRange;




    /** At a checkpoint the data fetching tasks for the next blocks are scheduled */
    protected long nextCheckpointOffset;


    protected long currentOffset;
    protected int maxReadAheadItemCount = 100;

    public DataStreamOverSliceWithCache(AdvancedRangeCacheImpl<A> cache, Range<Long> requestRange) {
        super();
        this.requestRange = requestRange;
        this.cache = cache;
        this.slice = cache.getSlice();
        this.pageRange = slice.newSliceAccessor();
        // requestRange.canonical(DiscreteDomain.longs()).lowerEndpoint();
        ContiguousSet<Long> set = ContiguousSet.create(requestRange, DiscreteDomain.longs());

        this.currentOffset = set.first(); // null or NSE exception?
        this.nextCheckpointOffset = currentOffset;
    }

    public long getNextCheckpointOffset() {
        return nextCheckpointOffset;
    }

    public SliceAccessor<A> getPageRange() {
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
        Preconditions.checkArgument(n >= 0, "Argument must not be negative");

        long start = nextCheckpointOffset;
        long end = start + n;

        Range<Long> claimAheadRange = Range.closedOpen(start, end);

        LockUtils.runWithLock(cache.getExecutorCreationReadLock(), () -> {
            LockUtils.runWithLock(slice.getReadWriteLock().readLock(), () -> {
                RangeSet<Long> gaps = slice.getGaps(claimAheadRange);
                processGaps(gaps, start, end);
            });
            nextCheckpointOffset = end;
        });

        clearPassedSlots();
    }

    // This map holds a single client's requested slots across all tasked workers
    protected Map<RangeRequestWorkerImpl<A>, Slot<Long>> workerToSlot = new IdentityHashMap<>();

    // protected LongSupplier offsetSupplier;
    protected long maxRedundantFetchSize = 1000;

    public DataStreamOverSliceWithCache(AdvancedRangeCacheImpl<A> cache, long nextCheckpointOffset, LongSupplier offsetSupplier) {
        this.cache = cache;
        this.slice = cache.getSlice();
        this.pageRange = slice.newSliceAccessor();
        this.nextCheckpointOffset = nextCheckpointOffset;
    }


    /**
     * Clear all slots of workers with a value less than the currentOffset.
     * These are workers for which we did not schedule any further tasks
     */
    public void clearPassedSlots() {
        Iterator<Slot<Long>> it = workerToSlot.values().iterator();
        while (it.hasNext()) {
            Slot<Long> slot = it.next();
            Long value = slot.getSupplier().get();

            if (value < currentOffset) {
                logger.debug("Clearing slot for offset " + slot.getSupplier().get() + " because current offset " + currentOffset + " is higher");
                slot.close();
                it.remove();
            }
        }
    }


    protected void scheduleWorkerToGaps(RangeSet<Long> gaps) {

        // Index workers by offset
        // If multiple workers have the same offset then only pick the first one with the highest request range

        Map<Long, RangeRequestWorkerImpl<A>> offsetToWorker = new HashMap<>();
        NavigableMap<Long, Long> offsetToEndpoint = new TreeMap<>();



        // for (RangeRequestWorker<T> e : workerToSlot.keySet()) {
        for (RangeRequestWorkerImpl<A> e : cache.getExecutors()) {
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
//        System.out.println("Gaps:             " + gaps);
//        System.out.println("Worker schedules: " + workerSchedules);

        for (Entry<Long, Long> schedule : workerSchedules.entrySet()) {
            long start = schedule.getKey();
            long end = schedule.getValue();
            long length = end - start;

            // Find the worker closed to the schedule start
            // SortedMap<Long, Long> headMap = offsetToEndpoint.headMap(start);
            Entry<Long, Long> workerRange = offsetToEndpoint.floorEntry(start);
            Long workerStart = workerRange == null || workerRange.getValue() < end
                    ? null
                    : workerRange.getKey();

            RangeRequestWorkerImpl<A> worker = workerStart == null
                    ? null
                    :  offsetToWorker.get(workerStart);

            // If there is no worker with that offset then create one
            // Otherwise update its slot
            if (worker == null) {
                Entry<RangeRequestWorkerImpl<A>, Slot<Long>> workerAndSlot = cache.newExecutor(start, length);
                workerToSlot.put(workerAndSlot.getKey(), workerAndSlot.getValue());
            } else {
                // Get or create a slot on the worker for publishing our end point demand
                Slot<Long> slot = workerToSlot.get(worker);

                if (slot == null) {
                    // We are reusing an existing worker for which we don't have a slot yet
                    slot = worker.newDemandSlot();
                    workerToSlot.put(worker, slot);
                }

                slot.set(end);
            }


        }

        // System.out.println("Worker schedules: " + workerSchedules);
    }

    protected void processGaps(RangeSet<Long> gaps, long start, long end) {
        scheduleWorkerToGaps(gaps);
    }

    @Override
    public boolean isOpen() {
        return !isClosed;
    }

    protected void closeActual() {
        logger.debug("Releasing slots: " + workerToSlot);
        workerToSlot.values().forEach(Slot::close);
        workerToSlot.clear();

        pageRange.close();
    }


    @Override
    public int read(A tgt, int tgtOffset, int length) throws IOException {
        ensureOpen();

        if (length == 0) {
            return 0;
        }

//        if (requestRange.upperEndpoint() == 4525) {
//            System.out.println("debug point");
//        }

        // Schedule data fetching for length + maxReadAheadItemCount items
        long requestedEndOffset = currentOffset + length;
        long readAheadEndOffset = requestedEndOffset + maxReadAheadItemCount;

        long maxAllowedEndOffset = LongMath.saturatedAdd(ContiguousSet.create(requestRange, DiscreteDomain.longs()).last(), 1);
        long effectiveRequestEndOffset = Math.min(requestedEndOffset, maxAllowedEndOffset);
        long effectiveReadAheadEndOffset = Math.min(readAheadEndOffset, maxAllowedEndOffset);

        // If we effectively can read zero bytes then we are at the end of data
        if (currentOffset >= effectiveRequestEndOffset) {
            return -1;
            // System.out.println("debug point");
        }

        Range<Long> totalReadRange = Range.closedOpen(currentOffset, effectiveRequestEndOffset);

        if (effectiveReadAheadEndOffset >= nextCheckpointOffset) {

            try {
                int numExtraItemsSinceLastCheckpoint = Ints.saturatedCast(effectiveReadAheadEndOffset - nextCheckpointOffset);

                // Read up length + maxReadAheadItemCount
                int n = Math.max(0, numExtraItemsSinceLastCheckpoint);

                // Increments nextCheckpointOffset by n
                checkpoint(n);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
         }


//        Preconditions.checkArgument(
//                offsetRange.encloses(totalReadRange),
//                "Read range  " + totalReadRange + " is not enclosed by claimed range " + offsetRange);


        int result;

        pageRange.claimByOffsetRange(currentOffset, effectiveReadAheadEndOffset);

        ReadWriteLock rwl = slice.getReadWriteLock();
        Lock readLock = rwl.readLock();
        readLock.lock();
        try {

            RangeSet<Long> loadedRanges = slice.getLoadedRanges();

            // FIXME - Add failed ranges again
            RangeMap<Long, List<Throwable>> failedRanges = TreeRangeMap.create(); // ;metaData.getFailedRanges();

            Range<Long> entry = null;
            List<Throwable> failures = null;

            // If the index is outside of the known size then abort
            // long knownSize = metaData.getSize();
            long maximumSize = slice.getMaximumKnownSize();
            if (currentOffset >= maximumSize) {
                // close();
                result = -1;
                // return -1;
            } else {

                // rangeBuffer.getFailedRanges().getEntry(currentIndex);

                failures = failedRanges.get(currentOffset); // .getEntry(currentIndex);
                entry = loadedRanges.rangeContaining(currentOffset);

                if (entry == null && failures == null) {
                    // Wait for data to become available
                    // Solution based on https://stackoverflow.com/questions/13088363/how-to-wait-for-data-with-reentrantreadwritelock

                    Lock writeLock = rwl.writeLock();
                    readLock.unlock();
                    writeLock.lock();

                    try {
                        long knownMaxSize;
                        // TODO We need to ensure the whole read range is covered
                        while ((entry = loadedRanges.rangeContaining(currentOffset)) == null &&
                                ((knownMaxSize = slice.getMaximumKnownSize()) < 0 || currentOffset < knownMaxSize)) {

                            boolean enableSanityCheck = false;
                            if (enableSanityCheck) {
                                TreeRangeSet<Long> sanityCheck = TreeRangeSet.create(loadedRanges.asRanges());
                                Range<Long> f = sanityCheck.rangeContaining(currentOffset);
                                System.out.println(String.format("%s.rangeContaining(%d) -> %s", sanityCheck, currentOffset, f));
                                if (entry == null && f != null) {
                                    throw new RuntimeException("bug in our range set view found: offset " + currentOffset + " incorrectly not in " + f);
                                }
                            }

                            try {
                                if (logger.isTraceEnabled()) {
                                    logger.trace(String.format(
                                            "Awaiting data at offset %d for entry %s of a slice of known max size %d with loaded ranges %s", currentOffset, entry, knownMaxSize, slice.getLoadedRanges()));
                                }

                                slice.getHasDataCondition().await();

                                if (logger.isTraceEnabled()) {
                                    logger.trace("Woke up after awaiting more data");
                                }
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } finally {
                        // rwl supports downgrading write lock to read by means of
                        // acquisition of read lock while write lock is held
                        readLock.lock();
                        writeLock.unlock();
                    }
                }
            }

            if (failures != null && !failures.isEmpty()) {
                throw new RuntimeException("Attempt to read a range of data marked with an error",
                        failures.get(0));
            }


            if (entry == null) {
                close();
                result = -1; // We were positioned at or past the end of data so there was nothing to read
            } else {
                Range<Long> range = totalReadRange.intersection(entry); //  entry; //.getKey();
                ContiguousSet<Long> cset = ContiguousSet.create(range, DiscreteDomain.longs());

                // Result is the length of the range of the available data
                int readLength = cset.size();

                if (readLength == 0) {
                    result = -1;
                } else {

                    long startAbs = cset.first();
                    long endAbs = startAbs + readLength;

                    // long rangeLength = endAbs - startAbs;

                    // TODO We may need to unlock before the claim in order to allow (re-loading) of the pages in the claim range

    //                pageRange.claimByOffsetRange(startAbs, endAbs);

                    result = pageRange.unsafeRead(tgt, tgtOffset, currentOffset, readLength);
                    if (result >= 0) {
                        currentOffset += result;
                    }
                }

                /*
                long pageSize = slice.getPageSize();
                long startPageId = PageUtils.getPageIndexForOffset(startAbs, pageSize);
                long endPageId = PageUtils.getPageIndexForOffset(endAbs, pageSize);
                long indexInPage = PageUtils.getIndexInPage(startAbs, pageSize);


                for (long i = startPageId; i <= endPageId; ++i) {
                    long endIndex = i == endPageId
                            ? PageUtils.getIndexInPage(endAbs, pageSize)
                            : pageSize;

                    RefFuture<BufferView<A>> currentPageRef = pageRange.getClaimedPages().get(i);

                    BufferView<A> buffer = currentPageRef.await();
                    buffer.getRangeBuffer().readInto(tgt, tgtOffset, indexInPage, Ints.checkedCast(endIndex));

                    indexInPage = 0;
                }
                */


//                pageRange.claimByOffsetRange(startAbs, endAbs);
//
//                BufferView<T> buffer = pageRange.getClaimedPages().firstEntry().getValue().await();
//                long capacity = buffer.getCapacity();
//                long endInPage = indexInPage + rangeLength;
//                int endIndex = Ints.saturatedCast(Math.min(capacity, endInPage));
//
//                List<T> list = buffer.getDataAsList();
//                List<T> subList = list.subList(indexInPage, endIndex);
//                rangeIterator = subList.iterator();

                // rangeIterator = IteratorUtils.limit(rangeBuffer.blockingIterator(start), length);


//                    rangeIterator = rangeBuffer.getBufferAsList().subList(range.lowerEndpoint(), range.upperEndpoint())
//                            .iterator();
            }

        } finally {
            readLock.unlock();
        }


        return result;
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return slice.getArrayOps();
    }


}

