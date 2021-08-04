package org.aksw.commons.rx.cache.range;

import java.util.Deque;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.aksw.commons.util.range.RangeBuffer;
import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;


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
public abstract class PageHelperBase<T>
{
    private static final Logger logger = LoggerFactory.getLogger(PageHelperBase.class);

    protected SmartRangeCacheImpl<T> cache;

    /**
     * The original request range by this request.
     * In general, the original request range has to be broken down into smaller ranges
     * because of result size limits of the backend
     */


    /** Do not send requests to the backend as long as that many items can be served from the cache */
    protected int maxReadAheadItemCount = 100;


    /**
     * Pages claimed so far by this iterator
     * Presently only the iterator claims pages on checkpoints
     * TODO Currently a concurrent map because it is under consideration to fire async
     *      events when pages are loaded.
     *
     */
    protected ConcurrentNavigableMap<Long, RefFuture<RangeBuffer<T>>> claimedPages = new ConcurrentSkipListMap<>();
    protected int currentIndex = -1;



    protected Runnable abortAction = null;
    protected boolean isAborted = false;

    /** The index of the next item to process */
    // protected long currentOffset;


    /** At a checkpoint the data fetching tasks for the next blocks are scheduled
      */
    protected long nextCheckpointOffset;


    public PageHelperBase(SmartRangeCacheImpl<T> cache, long nextCheckpointOffset) {
        super();
        this.cache = cache;
        this.nextCheckpointOffset = nextCheckpointOffset;
    }

//    public long getCurrentOffset() {
//        return currentOffset;
//    }

    public long getNextCheckpointOffset() {
        return nextCheckpointOffset;
    }


    public int getMaxReadAheadItemCount() {
        return maxReadAheadItemCount;
    }

    public SmartRangeCacheImpl<T> getCache() {
        return cache;
    }

    public ConcurrentNavigableMap<Long, RefFuture<RangeBuffer<T>>> getClaimedPages() {
        return claimedPages;
    }

    /**
     * Schedule ensured loading of the next 'n' items since the last
     * checkpoint.
     *
     * Check whether there are any gaps ahead that require
     * scheduling requests to the backend
     *
     */
    public void checkpoint(long n) throws Exception {

        long start = nextCheckpointOffset;
        long end = start + n;


        Range<Long> requestRange = Range.closedOpen(start, end);

        long pageSize = cache.getPageSize();

        // Check for any additional pages that need claiming
        long startPageId = cache.getPageIdForOffset(start);
        long endPageId = cache.getPageIdForOffset(end);

        // Remove all claimed pages before the checkpoint
        NavigableMap<Long, RefFuture<RangeBuffer<T>>> pagesToRelease = claimedPages.headMap(startPageId, false);
        pagesToRelease.values().forEach(Ref::close);
        pagesToRelease.clear();

        for (long i = startPageId; i <= endPageId; ++i) {
            claimedPages.computeIfAbsent(i, idx -> cache.getPageForPageId(idx));
        }


        NavigableMap<Long, RangeBuffer<T>> pages = LongStream.rangeClosed(startPageId, endPageId)
                .boxed()
                .collect(Collectors.toMap(
                    pageId -> pageId,
                    pageId -> {
                        try {
                            return claimedPages.get(pageId).await();
                        } catch (InterruptedException | ExecutionException e1) {
                            // TODO Improve handling
                            throw new RuntimeException(e1);
                        }
                    },
                    (u, v) -> { throw new RuntimeException("should not happen"); },
                    TreeMap::new));


        // Lock all pages
        try {
            pages.values().forEach(page -> page.getReadWriteLock().readLock().lock());

            // Prevent creation of new executors (other than by us) while we analyze the state
            cache.getExecutorCreationReadLock().lock();

            Deque<Range<Long>> gaps = SmartRangeCacheImpl.computeGaps(requestRange, pageSize, pages);

            processGaps(gaps, start, end);


        } finally {
            // Unlock all pages
            pages.values().forEach(page -> page.getReadWriteLock().readLock().unlock());

            cache.getExecutorCreationReadLock().unlock();

            nextCheckpointOffset += n;
        }
    }

    protected abstract void processGaps(Deque<Range<Long>> gaps, long start, long end);

    protected void closeActual() {
        isAborted = true;

        // Release all claimed pages
        // Remove all claimed pages before the checkpoint

        logger.debug("Releasing pages: " + claimedPages.keySet());

        claimedPages.values().forEach(Ref::close);
        claimedPages.clear();
        // TODO Release all claimed task-ranges
    }

    /**
     * Abort the request
     */
    public void close() {
        // Prevent creating an action after this method is called
        if (!isAborted) {
            synchronized (this) {
                if (!isAborted) {
                    closeActual();
                }
            }
        }
    }

}
