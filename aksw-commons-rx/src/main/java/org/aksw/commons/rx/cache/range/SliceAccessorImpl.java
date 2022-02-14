package org.aksw.commons.rx.cache.range;

import java.util.Deque;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;
import org.aksw.commons.util.range.BufferWithGenerationImpl;
import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

/**
 * A sequence of claimed ranges within a certain range, whereas the range
 * can be modified resulting in an incremental change of the claims.
 * An individual page range should only be operated by a single thread though
 * multiple threads may each have their own page range.
 * 
 * 
 * - claimByOffsetRange() only triggers loading of the pages but does not wait for them to become ready
 * - lock() waits for all claimed pages to become ready and afterwards locks them
 * - unlock() must be called after lock(); unlocks all pages
 * 
 * @author raven
 *
 * @param <T>
 */
public class SliceAccessorImpl<T>
    extends AutoCloseableWithLeakDetectionBase
    implements SliceAccessor<T>
{
    private static final Logger logger = LoggerFactory.getLogger(SliceAccessorImpl.class);

    // protected SmartRangeCacheImpl<T> cache;
    protected SliceWithPages<T> slice;


    protected Range<Long> offsetRange;
    protected ConcurrentNavigableMap<Long, RefFuture<BufferWithGenerationImpl<T>>> claimedPages = new ConcurrentSkipListMap<>();
    protected NavigableMap<Long, BufferWithGenerationImpl<T>> pageMap;
    protected boolean isLocked = false;

    public SliceAccessorImpl(SliceWithPages<T> cache) {
        super();
        this.slice = cache;
    }

    public Range<Long> getOffsetRange() {
        return offsetRange;
    }

//    public containsOffset(long offset) {
//    	offsetRange.
//    }

    public SliceWithPages<T> getCache() {
        return slice;
    }

//    @Override
    public ConcurrentNavigableMap<Long, RefFuture<BufferWithGenerationImpl<T>>> getClaimedPages() {
        return claimedPages;
    }

    @Override
    public void claimByOffsetRange(long startOffset, long endOffset) {
        // Check for any additional pages that need claiming
        long startPageId = slice.getPageIdForOffset(startOffset);
        long endPageId = slice.getPageIdForOffset(endOffset);

        offsetRange = Range.closedOpen(startOffset, endOffset);
        claimByPageIdRange(startPageId, endPageId);
    }

    protected synchronized void claimByPageIdRange(long startPageId, long endPageId) {
        ensureOpen();
        ensureUnlocked();

        // Remove any claimed page before startPageId
        NavigableMap<Long, RefFuture<BufferWithGenerationImpl<T>>> prefixPagesToRelease = claimedPages.headMap(startPageId, false);
        prefixPagesToRelease.values().forEach(Ref::close);
        prefixPagesToRelease.clear();

        // Remove any claimed page after endPageId
        NavigableMap<Long, RefFuture<BufferWithGenerationImpl<T>>> suffixPagesToRelease = claimedPages.tailMap(endPageId, false);
        suffixPagesToRelease.values().forEach(Ref::close);
        suffixPagesToRelease.clear();

        // TODO Consider efficiently skipping the prior range
//        long firstPageId = claimedPages.isEmpty() ? Long.MIN_VALUE, claimedPages.firstKey();
//        long lastPageId = claimedPages.isEmpty() ? Long.MAX_VALUE, claimedPages.lastKey();
//        long lowerEnd = Math.min(endPageId, lastPageId);

        for (long i = startPageId; i <= endPageId; ++i) {
            claimedPages.computeIfAbsent(i, idx -> {
                logger.debug("Acquired page item [" + idx + "]");
                RefFuture<BufferWithGenerationImpl<T>> page = slice.getPageForPageId(idx);

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

    protected NavigableMap<Long, BufferWithGenerationImpl<T>> computePageMap() {
        return claimedPages.entrySet().stream()
        .collect(Collectors.toMap(
            Entry::getKey,
            e -> {
                RefFuture<BufferWithGenerationImpl<T>> refFuture = e.getValue();

                return refFuture.await();
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

    public NavigableMap<Long, BufferWithGenerationImpl<T>> getPageMap() {
        updatePageMap();
        return pageMap;
    }


    protected void ensureUnlocked() {
        if (isLocked) {
            throw new IllegalStateException("Pages ware already locked - need to be unlocked first");
        }
    }

    @Override
    public void lock() {
        ensureUnlocked();

        isLocked = true;
        updatePageMap();
//        pageMap.values().forEach(page -> page.getReadWriteLock().readLock().lock());

        // Prevent creation of new executors (other than by us) while we analyze the state
        // slice.getWorkerCreationLock().lock();
    }

//    @Override
//    public Deque<Range<Long>> getGaps() {
//        updatePageMap();
//
//        int pageSize = cache.getPageSize();
//        Deque<Range<Long>> result = SmartRangeCacheImpl.computeGaps(offsetRange, pageSize, pageMap);
//        return result;
//    }

    @Override
    public void unlock() {
        // Unlock all pages
        updatePageMap();
        // pageMap.values().forEach(page -> page.getReadWriteLock().readLock().unlock());

        // slice.getWorkerCreationLock().unlock();
        isLocked = false;
    }

    @Override
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


    /** The number of items to process in one batch (before checking for conditions such as interrupts or no-more-demand) */
    protected int bulkSize = 16;


    @Override
    public synchronized void putAll(long offset, Object arrayWithItemsOfTypeT, int arrOffset, int arrLength) {

        ensureOpen();

        Range<Long> totalWriteRange = Range.closedOpen(offset, offset + arrLength);
        Preconditions.checkArgument(
                offsetRange.encloses(totalWriteRange),
                "Write range  " + totalWriteRange + " is not enclosed by claimed range " + offsetRange);


        try (RefFuture<SliceMetaData> ref = slice.getMetaData()) {
            SliceMetaData metaData = ref.await();
            Lock metaDataWriteLock = metaData.getReadWriteLock().writeLock();
            metaDataWriteLock.lock();
            try {

                long knownSize = metaData.getKnownSize();

                int remaining = arrLength;
                while (remaining > 0) {

                    long pageId = slice.getPageIdForOffset(offset);
                    long offsetInPage = slice.getIndexInPageForOffset(offset);

                    RefFuture<BufferWithGenerationImpl<T>> currentPageRef = getClaimedPages().get(pageId);

                    BufferWithGenerationImpl<T> buffer = currentPageRef.await();

        //            BulkingSink<T> sink = new BulkingSink<>(bulkSize,
        //                    (arr, start, len) -> BufferWithGeneration.putAll(offsetInPage, arr, start, len));

                    long numItemsUntilPageEnd = buffer.getCapacity() - offsetInPage;
                    // long numItemsUntilPageKnownSize = cache.getMetaData().getMaximumKnownSize(); // BufferWithGeneration.getKnownSize() >= 0 ? BufferWithGeneration.getKnownSize() : BufferWithGeneration.getCapacity();

                    long numItemsUntilPageKnownSize = knownSize < 0 ? Long.MAX_VALUE : knownSize - offset;

                    // long numItemsUtilRequestLimit = (requestOffset + requestLimit) - offset;

                    int limit = Math.min(Ints.saturatedCast(Math.min(
                            numItemsUntilPageEnd,
                            numItemsUntilPageKnownSize)),
                            remaining);

                    Lock contentWriteLock = buffer.getReadWriteLock().writeLock();
                    contentWriteLock.lock();

                    try {
                        buffer.putAll(offsetInPage, arrayWithItemsOfTypeT, arrOffset, limit);
                    } finally {
                        contentWriteLock.unlock();
                    }
                    remaining -= limit;
                    offset += limit;
                    arrOffset += limit;
                }

                long min = metaData.getMinimumKnownSize();
                metaData.setMinimumKnownSize(Math.max(min, offset + arrLength));
                metaData.getLoadedRanges().add(totalWriteRange);
                metaData.getHasDataCondition().signalAll();
            } finally {
                metaDataWriteLock.unlock();
            }
        }

    }

    @Override
    protected void closeActual() {
        releaseAll();
    }

}
