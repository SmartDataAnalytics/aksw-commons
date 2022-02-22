package org.aksw.commons.rx.cache.range;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;
import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;
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
 * @param <A>
 */
public class SliceAccessorImpl<A>
    extends AutoCloseableWithLeakDetectionBase
    implements SliceAccessor<A>
{
    private static final Logger logger = LoggerFactory.getLogger(SliceAccessorImpl.class);

    // protected SmartRangeCacheImpl<T> cache;
    protected SliceWithPages<A> slice;


    protected Range<Long> offsetRange;
    protected ConcurrentNavigableMap<Long, RefFuture<BufferView<A>>> claimedPages = new ConcurrentSkipListMap<>();
    // protected NavigableMap<Long, BufferView<A>> pageMap;
    protected boolean isLocked = false;

    public SliceAccessorImpl(SliceWithPages<A> cache) {
        super();
        this.slice = cache;
    }

    public Range<Long> getOffsetRange() {
        return offsetRange;
    }

//    public containsOffset(long offset) {
//    	offsetRange.
//    }

    public SliceWithPages<A> getCache() {
        return slice;
    }

//    @Override
    public ConcurrentNavigableMap<Long, RefFuture<BufferView<A>>> getClaimedPages() {
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
        NavigableMap<Long, RefFuture<BufferView<A>>> prefixPagesToRelease = claimedPages.headMap(startPageId, false);
        prefixPagesToRelease.values().forEach(Ref::close);
        prefixPagesToRelease.clear();

        // Remove any claimed page after endPageId
        NavigableMap<Long, RefFuture<BufferView<A>>> suffixPagesToRelease = claimedPages.tailMap(endPageId, false);
        suffixPagesToRelease.values().forEach(Ref::close);
        suffixPagesToRelease.clear();

        // TODO Consider efficiently skipping the prior range
//        long firstPageId = claimedPages.isEmpty() ? Long.MIN_VALUE, claimedPages.firstKey();
//        long lastPageId = claimedPages.isEmpty() ? Long.MAX_VALUE, claimedPages.lastKey();
//        long lowerEnd = Math.min(endPageId, lastPageId);

        
        // Phase 1/2: Trigger loading of all pages
        for (long i = startPageId; i <= endPageId; ++i) {
            claimedPages.computeIfAbsent(i, idx -> {
                logger.debug("Acquired page item [" + idx + "]");
                RefFuture<BufferView<A>> page = slice.getPageForPageId(idx);
                
                // System.out.println("Loaded page " + idx);
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

        
        // Phase 2/2: Await for all pages to be loaded - this may also resync them
		for (RefFuture<BufferView<A>> page : claimedPages.values()) {
        	page.await();
        }
    }

    protected NavigableMap<Long, BufferView<A>> computePageMap() {
        return claimedPages.entrySet().stream()
        .collect(Collectors.toMap(
            Entry::getKey,
            e -> {
                RefFuture<BufferView<A>> refFuture = e.getValue();

                return refFuture.await();
            },
            (u, v) -> { throw new RuntimeException("should not happen"); },
            TreeMap::new));
    }

    // Deque<Range<Long>> gaps = SmartRangeCacheImpl.computeGaps(requestRange, pageSize, pages);


//    protected void updatePageMap() {
//        if (pageMap == null) {
//            pageMap = computePageMap();
//        }
//    }
//
//    public NavigableMap<Long, BufferView<A>> getPageMap() {
//        updatePageMap();
//        return pageMap;
//    }


    protected void ensureUnlocked() {
        if (isLocked) {
            throw new IllegalStateException("Pages ware already locked - need to be unlocked first");
        }
    }

    @Override
    public void lock() {
        ensureUnlocked();

        isLocked = true;
        // updatePageMap();
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
        // updatePageMap();
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
        // pageMap = null;
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


//        try (RefFuture<SliceMetaData> ref = slice.getMetaData()) {
//            SliceMetaData metaData = ref.await();
//            Lock metaDataWriteLock = metaData.getReadWriteLock().writeLock();
//            metaDataWriteLock.lock();
//            try {

        Lock lock = slice.getReadWriteLock().writeLock();
        lock.lock();
        try {
            long knownSize = slice.getKnownSize();

            int remaining = arrLength;
            while (remaining > 0) {

                long pageId = slice.getPageIdForOffset(offset);
                long offsetInPage = slice.getIndexInPageForOffset(offset);

                RefFuture<BufferView<A>> currentPageRef = getClaimedPages().get(pageId);

                BufferView<A> buffer = currentPageRef.await();

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
                    buffer.getRangeBuffer().putAll(offsetInPage, arrayWithItemsOfTypeT, arrOffset, limit);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    contentWriteLock.unlock();
                }
                remaining -= limit;
                offset += limit;
                arrOffset += limit;
            }

            slice.updateMinimumKnownSize(offset);
//            long min = slice.getMinimumKnownSize();
//            slice.setMinimumKnownSize(Math.max(min, offset + arrLength));
            slice.getLoadedRanges().add(totalWriteRange);
            slice.getHasDataCondition().signalAll();

        } finally {
            lock.unlock();
        }
    }

    
    /** Read a range of data - does not await any new data */
    public int unsafeRead(A tgt, int tgtOffset, long srcOffset, int length) throws IOException {
        ensureOpen();

        Range<Long> totalReadRange = Range.closedOpen(srcOffset, srcOffset + length);
        Preconditions.checkArgument(
                offsetRange.encloses(totalReadRange),
                "Read range  " + totalReadRange + " is not enclosed by claimed range " + offsetRange);


        int result;

        // Range<Long> range = totalReadRange.intersection(entry); //  entry; //.getKey();
        ContiguousSet<Long> cset = ContiguousSet.create(totalReadRange, DiscreteDomain.longs());

        // Result is the length of the range
        result = cset.size();

        long startAbs = cset.first();
        long endAbs = startAbs + result;

        // long rangeLength = endAbs - startAbs;

        long pageSize = slice.getPageSize();
        long startPageId = PageUtils.getPageIndexForOffset(startAbs, pageSize);
        long indexInPage = PageUtils.getIndexInPage(startAbs, pageSize);

        int remainingInSrc = length;
        for (long i = startPageId; remainingInSrc > 0; ++i) {
            RefFuture<BufferView<A>> currentPageRef = getClaimedPages().get(i);

            BufferView<A> buffer = currentPageRef.await();
			int remainingInPage = Ints.checkedCast(Math.min(pageSize - indexInPage, remainingInSrc));

			buffer.getRangeBuffer().readInto(tgt, tgtOffset, indexInPage, remainingInPage);

			tgtOffset += remainingInPage;
			remainingInSrc -= remainingInPage;
			indexInPage = 0;
        }

        return result;
    }

    /**
     * Method is subject to removal - use sequentialReaderForSlice.read
     * 
     * The range [srcOffset, srcOffset + length) must be within the claimed range!
     * @throws IOException
     *
     */
    // @Override
    public int blockingRead(A tgt, int tgtOffset, long srcOffset, int length) throws IOException {
        ensureOpen();

        Range<Long> totalReadRange = Range.closedOpen(srcOffset, srcOffset + length);
        Preconditions.checkArgument(
                offsetRange.encloses(totalReadRange),
                "Read range  " + totalReadRange + " is not enclosed by claimed range " + offsetRange);


        int result;

        long currentOffset = srcOffset;
        ReadWriteLock rwl = slice.getReadWriteLock();
        Lock readLock = rwl.readLock();
        readLock.lock();
        try {

            RangeSet<Long> loadedRanges = slice.getLoadedRanges();

            // FIXME - Add failed ranges again
            RangeMap<Long, List<Throwable>> failedRanges = TreeRangeMap.create(); // ;metaData.getFailedRanges();

            Range<Long> entry = null;
            List<Throwable> failures = null;

            try {
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
                            long knownSize;
                            while ((entry = loadedRanges.rangeContaining(currentOffset)) == null &&
                                    ((knownSize = slice.getMaximumKnownSize()) < 0 || currentOffset < knownSize)) {
                                try {
                                    logger.info("Awaiting more data: " + entry + " " + currentOffset + " " + knownSize);
                                    slice.getHasDataCondition().await();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } finally {
                            writeLock.unlock();
                            readLock.lock();
                        }
                    }
                }
            } finally {
                readLock.unlock();
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

                // Result is the length of the range
                result = cset.size();

                long startAbs = cset.first();
                long endAbs = startAbs + result;

                // long rangeLength = endAbs - startAbs;

                long pageSize = slice.getPageSize();
                long startPageId = PageUtils.getPageIndexForOffset(startAbs, pageSize);
                long endPageId = PageUtils.getPageIndexForOffset(endAbs, pageSize);
                long indexInPage = PageUtils.getIndexInPage(startAbs, pageSize);

                for (long i = startPageId; i <= endPageId; ++i) {
                    long endIndex = i == endPageId
                            ? PageUtils.getIndexInPage(endAbs, pageSize)
                            : pageSize;

                    RefFuture<BufferView<A>> currentPageRef = getClaimedPages().get(i);

                    BufferView<A> buffer = currentPageRef.await();
                    buffer.getRangeBuffer().readInto(tgt, tgtOffset, indexInPage, Ints.checkedCast(endIndex));

                    indexInPage = 0;
                }


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
    protected void closeActual() {
        releaseAll();
    }

}
