package org.aksw.commons.rx.cache.range;

import java.util.Deque;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.aksw.commons.util.range.RangeBuffer;
import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;

/**
 * A sequence of claimed ranges within a certain range, whereas the range
 * can be modified resulting in an incremental change of the claims.
 *
 * @author raven
 *
 * @param <T>
 */
public class PageRange<T> {
    private static final Logger logger = LoggerFactory.getLogger(PageRange.class);

    protected SmartRangeCacheImpl<T> cache;

    protected Range<Long> offsetRange;
    protected ConcurrentNavigableMap<Long, RefFuture<RangeBuffer<T>>> claimedPages = new ConcurrentSkipListMap<>();
    protected NavigableMap<Long, RangeBuffer<T>> pageMap;
    protected boolean isLocked = false;

    public PageRange(SmartRangeCacheImpl<T> cache) {
        super();
        this.cache = cache;
    }

    public Range<Long> getOffsetRange() {
        return offsetRange;
    }

//    public containsOffset(long offset) {
//    	offsetRange.
//    }

    public SmartRangeCacheImpl<T> getCache() {
        return cache;
    }

    public ConcurrentNavigableMap<Long, RefFuture<RangeBuffer<T>>> getClaimedPages() {
        return claimedPages;
    }

    public void claimByOffsetRange(long startOffset, long endOffset) {
        // Check for any additional pages that need claiming
        long startPageId = cache.getPageIdForOffset(startOffset);
        long endPageId = cache.getPageIdForOffset(endOffset);

        claimByPageIdRange(startPageId, endPageId);
    }

    protected void claimByPageIdRange(long startPageId, long endPageId) {
        ensureUnlocked();

        // Remove any claimed page before startPageId
        NavigableMap<Long, RefFuture<RangeBuffer<T>>> prefixPagesToRelease = claimedPages.headMap(startPageId, false);
        prefixPagesToRelease.values().forEach(Ref::close);
        prefixPagesToRelease.clear();

        // Remove any claimed page after endPageId
        NavigableMap<Long, RefFuture<RangeBuffer<T>>> suffixPagesToRelease = claimedPages.tailMap(endPageId, false);
        suffixPagesToRelease.values().forEach(Ref::close);
        suffixPagesToRelease.clear();

        // TODO Consider efficiently skipping the prior range
//        long firstPageId = claimedPages.isEmpty() ? Long.MIN_VALUE, claimedPages.firstKey();
//        long lastPageId = claimedPages.isEmpty() ? Long.MAX_VALUE, claimedPages.lastKey();
//        long lowerEnd = Math.min(endPageId, lastPageId);

        for (long i = startPageId; i <= endPageId; ++i) {
            claimedPages.computeIfAbsent(i, idx -> {
                RefFuture<RangeBuffer<T>> page = cache.getPageForPageId(idx);

//                if (isLocked) {
//                    try {
//                        page.await().getReadWriteLock().readLock().lock();
//                    } catch (InterruptedException | ExecutionException e) {
//                        throw new RuntimeException(e);
//                    }
//                }

                return page;
            });
        }

        pageMap = null;
    }

    protected NavigableMap<Long, RangeBuffer<T>> computePageMap() {
        return claimedPages.entrySet().stream()
        .collect(Collectors.toMap(
            Entry::getKey,
            e -> {
                RefFuture<RangeBuffer<T>> refFuture = e.getValue();

                try {
                    return refFuture.await();
                } catch (InterruptedException | ExecutionException e1) {
                    // TODO Improve handling
                    throw new RuntimeException(e1);
                }
            },
            (u, v) -> { throw new RuntimeException("should not happen"); },
            TreeMap::new));
  }

    // Deque<Range<Long>> gaps = SmartRangeCacheImpl.computeGaps(requestRange, pageSize, pages);


    protected void updatePageMap() {
        if (pageMap == null) {
            pageMap = computePageMap();
        }
    }

    public NavigableMap<Long, RangeBuffer<T>> getPageMap() {
        updatePageMap();
        return pageMap;
    }


    protected void ensureUnlocked() {
        if (isLocked) {
            throw new IllegalStateException("Pages ware already locked - need to be unlocked first");
        }
    }

    public void lock() {
        ensureUnlocked();

        isLocked = true;
        pageMap.values().forEach(page -> page.getReadWriteLock().readLock().lock());

        // Prevent creation of new executors (other than by us) while we analyze the state
        cache.getExecutorCreationReadLock().lock();
    }

    public Deque<Range<Long>> getGaps() {
        int pageSize = cache.getPageSize();
        Deque<Range<Long>> result = SmartRangeCacheImpl.computeGaps(offsetRange, pageSize, pageMap);
        return result;
    }

    public void unlock() {
        // Unlock all pages
        pageMap.values().forEach(page -> page.getReadWriteLock().readLock().unlock());

        cache.getExecutorCreationReadLock().unlock();
        isLocked = false;
    }

    public void releaseAll() {
        if (isLocked) {
            unlock();
        }

        // Release all claimed pages
        // Remove all claimed pages before the checkpoint

        logger.debug("Releasing pages: " + claimedPages.keySet());

        claimedPages.values().forEach(Ref::close);
        claimedPages.clear();
        pageMap = null;
        // TODO Release all claimed task-ranges
    }
}
