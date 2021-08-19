package org.aksw.commons.rx.cache.range;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.math.LongMath;
import com.google.common.primitives.Ints;


/**
 * The class drives the iteration of items from the cache
 * and creates background data fetching processes as needed.
 *
 * Thereby this class does not fetch the data directly, but it declares
 * interest in data ranges. The SmartRangeCache will schedule loading of the region
 * at least as long as interest is expressed.
 *
 * @author raven
 *
 * @param <T>
 */
public class RangeRequestIterator<T>
    extends AbstractIterator<T>
{
    private static final Logger logger = LoggerFactory.getLogger(RangeRequestIterator.class);

    protected PageHelperForConsumer<T> pageHelper;
    protected SmartRangeCacheImpl<T> cache;

    /**
     * The original request range by this request.
     * In general, the original request range has to be broken down into smaller ranges
     * because of result size limits of the backend
     */
    protected Range<Long> requestRange;

    protected SliceWithPagesIterator<T> currentPageIt = null;

    // protected int currentIndex = -1;

    /** The index of the next item to read */
    protected long currentOffset;


    protected int maxReadAheadItemCount = 100;


    public RangeRequestIterator(SmartRangeCacheImpl<T> cache, Range<Long> requestRange) { //SmartRangeCacheImpl<T> cache, Range<Long> requestRange) {
        super();
        this.cache = cache;
        this.requestRange = requestRange;

        long nextCheckpointOffset = ContiguousSet.create(requestRange, DiscreteDomain.longs()).first();

        this.currentOffset = nextCheckpointOffset;
        this.pageHelper = new PageHelperForConsumer<>(cache, nextCheckpointOffset, this::getCurrentOffset);
        //currentOffset = pageHelper.getNextCheckpointOffset(); // nextCheckpointOffset = ContiguousSet.create(requestRange, DiscreteDomain.longs()).first();
    }

    public long getCurrentOffset() {
        return currentOffset;
    }


    @Override
    protected T computeNext() {
        if (currentOffset == pageHelper.getNextCheckpointOffset()) {
            try {
                long end = ContiguousSet.create(requestRange, DiscreteDomain.longs()).last();
                int numItemsUntilRequestRangeEnd = Ints.saturatedCast(LongMath.saturatedAdd(end - currentOffset, 1));

                int n = Math.min(maxReadAheadItemCount, numItemsUntilRequestRangeEnd);

                // Increments nextCheckpointOffset by n
                pageHelper.checkpoint(n);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
         }

        T result;

        if (requestRange.contains(currentOffset)) {
            if (currentPageIt == null || !currentPageIt.hasNext()) {

                if (currentPageIt != null) {
                    currentPageIt.close();
                }

                currentPageIt = new SliceWithPagesIterator<>(cache.getSlice(), currentOffset);

//                // long pageId = cache.getPageIdForOffset(currentOffset);
//                RangeBuffer<T> currentPage;
//                try {
//                    long pageId = cache.getSlice().getPageIdForOffset(currentOffset);
//                    ConcurrentNavigableMap<Long, RefFuture<RangeBuffer<T>>> claimedPages = pageHelper.getPageRange().getClaimedPages();
//                    RefFuture<RangeBuffer<T>> pageRefFuture = claimedPages.get(pageId);
//
//                    if (pageRefFuture == null) {
//                        System.err.println("DEBUG POINT");
//                    }
//
//                    currentPage = pageRefFuture.await();
//                    // currentPage = claimedPages.pollFirstEntry().getValue().await();
//                } catch (InterruptedException | ExecutionException e) {
//                    throw new RuntimeException(e);
//                }
//
//                currentIndex = cache.getSlice().getIndexInPageForOffset(currentOffset);
//                currentPageIt = currentPage.blockingIterator(currentIndex);

                // ISSUE A blocking iterator obtained this way is backed by its own redundant PageRange instance
//                currentPageIt = cache.getSlice().blockingIterator(currentOffset);
            }

            if (currentPageIt.hasNext()) {
                result = currentPageIt.next();

                // Assert non null here?
                // A likely reason for null values is an inconsistency between (range-)metadata and (buffer-)content

                // ++currentIndex;
                ++currentOffset;
            } else {
                close();
                result = endOfData();
            }

        } else {
            close();
            result = endOfData();
        }


        return result;
    }


    /**
     * Abort the request
     */
    public void close() {

        if (currentPageIt != null) {
            currentPageIt.close();
        }

        pageHelper.close();
    }

}
