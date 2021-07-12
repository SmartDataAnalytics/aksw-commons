package org.aksw.commons.rx.cache.range;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.aksw.commons.collections.PrefetchIterator;
import org.aksw.commons.util.range.RangeBuffer;
import org.aksw.commons.util.range.RangeBufferImpl;
import org.aksw.commons.util.range.RangeUtils;
import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefFuture;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.google.common.math.LongMath;


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
public class RequestIterator<T>
    extends AbstractIterator<T>
{
    protected SmartRangeCacheImpl<T> cache;

    /**
     * The original request range by this request.
     * In general, the original request range has to be broken down into smaller ranges
     * because of result size limits of the backend
     */
    protected Range<Long> requestRange;


    /** Do not send requests to the backend as long as that many items can be served from the cache */
    protected long readAheadItemCount = 100;


    /** Pages claimed so far by this iterator;
     * new pages will be added to this map asynchronously when they become available */
    protected ConcurrentNavigableMap<Long, RefFuture<RangeBuffer<T>>> claimedPages = new ConcurrentSkipListMap<>();

    /** The reference to the current page */
    protected RefFuture<RangeBufferImpl<T>> currentPageRef = null;
    protected Iterator<T> currentPageIt = null;


    protected int currentIndex = -1;



    protected Runnable abortAction = null;
    protected boolean isAborted = false;

    /** The index of the next item to read */
    protected long currentOffset;

    protected long claimAheadLength;



    /**
     * In order to deal with large or infinite request ranges, request processed in blocks:
     *  - Only pages within the block range are claimed.
     *  - Upon reading a percentage of the block a checkpoint is made that prepares the next block
     *  - Data fetching is scheduled for any gaps in the block
     */
    protected long blockLength;

    /** At a checkpoint the data fetching tasks for the next blocks are scheduled
      */
    protected long nextCheckpointOffset = 0;


    public RequestIterator(SmartRangeCacheImpl<T> cache) {
        super();
        this.cache = cache;
    }


    /**
     * The claim ahead range starts at the current offset and has length claimAheadLimit
     */
    protected Range<Long> getClaimAheadRange() {
        // TODO Use -1 or Long.MAX_VALUE for unbounded case?
        return Range.closedOpen(currentOffset, currentOffset + claimAheadLength);
    }


    protected void onPageLoaded(long offset, RefFuture<RangeBuffer<T>> content) {
        Range<Long> claimAheadRange = getClaimAheadRange();

        if (claimAheadRange.contains(offset)) {
            claimedPages.put(offset, content.acquire());
        }
    }




    protected void init() {
//		RangeCache<Long, RangeBuffer<T>> pageCache = cache.pageCache;
//		synchronized (pageCache) {
//			RangeMap<Long, Ref<RangeBuffer<T>>> claims = pageCache.claimAll(requestRange);

//			RangeSet<Long> ranges = claims.asMapOfRanges().keySet();
//			RangeSet<Long> gaps = RangeUtils.gaps(ranges, requestRange);


//		}

        // cache.activeRequests.add(this);
        synchronized (this) {
            if (!isAborted) {
                abortAction = cache.register(this);
            }
        }
    }


    public RefFuture<RangeBuffer<T>> getPage(long pageId) {
        return claimedPages.computeIfAbsent(pageId, idx -> cache.getPageForPageId(idx));
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

        List<RangeRequestExecutor<T>> candExecutors = new ArrayList<>();


        long start = nextCheckpointOffset;
        long end = start + n;


        Range<Long> requestRange = Range.closedOpen(start, end);

        long pageSize = cache.getPageSize();

        // Check for any additional pages that need claiming
        long startPageId = cache.getPageIdForOffset(start);
        long endPageId = cache.getPageIdForOffset(end);

        // Remove all claimed pages before the checkpoint
        claimedPages.headMap(startPageId, false).clear();

        for (long i = startPageId; i < endPageId; ++i) {
            claimedPages.computeIfAbsent(i, idx -> cache.getPageForPageId(idx));
            cache.getPageForOffset(i);
        }


        NavigableMap<Long, RangeBuffer<T>> pages = LongStream.range(startPageId, endPageId)
                .boxed()
                .collect(Collectors.toMap(
                    pageId -> pageId * pageSize,
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

            // Range<Long> totalRange = Range.closedOpen(pageOffset, pageOffset + pageSize);


            RangeSet<Long> loadedRanges = TreeRangeSet.create();
            pages.entrySet().stream().flatMap(e -> e.getValue().getLoadedRanges().asMapOfRanges().keySet().stream().map(range ->
                    RangeUtils.apply(
                            range,
                            e.getKey() * pageSize,
                            (endpoint, value) -> LongMath.saturatedAdd(endpoint, (long)value)))
                )
                .forEach(loadedRanges::add);


            RangeSet<Long> gaps = RangeUtils.gaps(requestRange, loadedRanges);
            Iterator<Range<Long>> gapIt = gaps.asRanges().iterator();


            // Prevent creation of new executors while we analyze the state
            cache.getExecutorCreationReadLock().lock();


            candExecutors.addAll(cache.getExecutors());

            Range<Long> gap = null;
            while (gapIt.hasNext()) {
                 gap = gapIt.next();

                // Remove candidate executors that cannot serve the gap
                // In the worst case no executors remain - in that case
                // we have to create a new executor that starts after the last
                // position that can be served by one of the current executors
                List<RangeRequestExecutor<T>> nextCandExecutors = new ArrayList<>(candExecutors.size());
                for (RangeRequestExecutor<T> candExecutor : candExecutors) {
                    Range<Long> ewr = candExecutor.getWorkingRange();
                    if (ewr.encloses(gap)) {
                        nextCandExecutors.add(candExecutor);
                    }
                }

                // No executor could deal with all the gaps in the read ahead range
                // Abort the search
                if (nextCandExecutors.isEmpty()) {
                    break;
                }
            }


            // TODO Create an executor...

            // Register the request range to it


        } finally {
            // Unlock all pages
            pages.values().forEach(page -> page.getReadWriteLock().readLock().unlock());

            cache.getExecutorCreationReadLock().unlock();
        }
    }


    @Override
    protected T computeNext() {
        if (currentOffset == nextCheckpointOffset) {
            try {
                checkpoint(readAheadItemCount);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
         }

        // RangeBuffer<T> currentPage = null;

        if (currentPageRef == null) {
            long pageId = cache.getPageIdForOffset(currentOffset);
            RangeBuffer<T> currentPage;
            try {
                currentPage = getPage(pageId).await();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            currentIndex = cache.getIndexInPageForOffset(currentOffset);
            currentPageIt = currentPage.get(currentIndex);
        }

        T result = currentPageIt.next();

        ++currentIndex;
        ++currentOffset;

        return result;
//
//
//        // Get the page at offset, then return an iterator over the page's items
//        // That iterator will block if items have not yet been loadded
//        // Once all items of a page have been iterated, release the page and increment
//        // the offset
//
//        long nextGapOffset = 0;
//
//
//        return null;
    }

    /**
     * Abort the request
     */
    public void close() {
        // Prevent creating an action after this method is called
        if (!isAborted) {
            synchronized (this) {
                if (!isAborted) {
                    isAborted = true;

                    // Release all claimed pages
                    claimedPages.forEach((k, v) -> v.close());

                    // TODO Release all claimed task-ranges

                }
            }
        }
    }

}
