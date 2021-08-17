package org.aksw.commons.rx.cache.range;

import java.util.Deque;

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

    protected PageRange<T> pageRange;
    protected boolean isAborted = false;


    /** At a checkpoint the data fetching tasks for the next blocks are scheduled */
    protected long nextCheckpointOffset;


    public PageHelperBase(PageRange<T> pageRange, long nextCheckpointOffset) {
        super();
        this.pageRange = pageRange;
        this.nextCheckpointOffset = nextCheckpointOffset;
    }

    public long getNextCheckpointOffset() {
        return nextCheckpointOffset;
    }

    public PageRange<T> getPageRange() {
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
    public void checkpoint(long n) throws Exception {

        long start = nextCheckpointOffset;
        long end = start + n;

        pageRange.claimByOffsetRange(start, end);
                // Lock all pages
        try {
            pageRange.lock();

            Deque<Range<Long>> gaps = pageRange.getGaps();
            processGaps(gaps, start, end);
        } finally {
            pageRange.unlock();

            nextCheckpointOffset += n;
        }
    }

    protected abstract void processGaps(Deque<Range<Long>> gaps, long start, long end);

    protected void closeActual() {
        pageRange.releaseAll();
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

//
//
//public void claimPageIdRange(long startPageId, long endPageId) {
//    // Remove any claimed page before startPageId
//    NavigableMap<Long, RefFuture<RangeBuffer<T>>> prefixPagesToRelease = claimedPages.headMap(startPageId, false);
//    prefixPagesToRelease.values().forEach(Ref::close);
//    prefixPagesToRelease.clear();
//
//    // Remove any claimed page after endPageId
//    NavigableMap<Long, RefFuture<RangeBuffer<T>>> suffixPagesToRelease = claimedPages.tailMap(endPageId, false);
//    suffixPagesToRelease.values().forEach(Ref::close);
//    suffixPagesToRelease.clear();
//
//
//    for (long i = startPageId; i <= endPageId; ++i) {
//        claimedPages.computeIfAbsent(i, idx -> cache.getPageForPageId(idx));
//    }
//}

//
//public Stream<Entry<Long, RangeBuffer<T>>> getPageStream() {
//    return claimedPages.entrySet().stream()
//            .map(e -> {
//                Long pageId = e.getKey();
//                RangeBuffer<T> buffer;
//                try {
//                    buffer = claimedPages.get(pageId).await();
//                } catch (InterruptedException | ExecutionException e1) {
//                    // TODO Improve handling
//                    throw new RuntimeException(e1);
//                }
//                return new SimpleEntry<>(pageId, buffer);
//            });
//}
//
//
//protected NavigableMap<Long, RangeBuffer<T>> computePageMap2() {
//    return getPageStream().collect(
//            Collectors.toMap(
//                  Entry::getKey,
//                  Entry::getValue,
//                  (u, v) -> { throw new RuntimeException("should not happen"); },
//                  TreeMap::new));
//}

//protected void updatePageMap() {
//    if (pageMap == null) {
//        pageMap = computePageMap();
//    }
//}
//
//protected NavigableMap<Long, RangeBuffer<T>> computePageMap() {
//      return claimedPages.entrySet().stream()
//      .collect(Collectors.toMap(
//          Entry::getKey,
//          e -> {
//              Long pageId = e.getKey();
//              RefFuture<RangeBuffer<T>> refFuture = e.getValue();
//
//              try {
//                  return refFuture.await();
//              } catch (InterruptedException | ExecutionException e1) {
//                  // TODO Improve handling
//                  throw new RuntimeException(e1);
//              }
//          },
//          (u, v) -> { throw new RuntimeException("should not happen"); },
//          TreeMap::new));
//}
//
//
//
//
// public void lockCurrentRange() {
//     updatePageMap();
//     pageMap.values().forEach(page -> page.getReadWriteLock().readLock().lock());
//
//     // Prevent creation of new executors (other than by us) while we analyze the state
//     cache.getExecutorCreationReadLock().lock();
// }
//
// public void unlockCurrentRange() {
//     // Unlock all pages
//     pages.values().forEach(page -> page.getReadWriteLock().readLock().unlock());
//
//     cache.getExecutorCreationReadLock().unlock();
// }
//
//