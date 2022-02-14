package org.aksw.commons.rx.cache.range;

import java.util.concurrent.locks.Lock;

import org.aksw.commons.lock.LockUtils;
import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;
import org.aksw.commons.util.ref.RefFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;


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
    extends AutoCloseableWithLeakDetectionBase
{
    private static final Logger logger = LoggerFactory.getLogger(PageHelperBase.class);

    protected SliceWithAutoSync<T> slice;
    protected SmartRangeCacheImpl<T> cache;
    protected SliceAccessor<T> pageRange;


    /** At a checkpoint the data fetching tasks for the next blocks are scheduled */
    protected long nextCheckpointOffset;


    public PageHelperBase(SmartRangeCacheImpl<T> cache, long nextCheckpointOffset) {
        super();
        this.cache = cache;
        this.slice = cache.getSlice();
        this.pageRange = slice.newSliceAccessor();
        this.nextCheckpointOffset = nextCheckpointOffset;
    }

    public long getNextCheckpointOffset() {
        return nextCheckpointOffset;
    }

    public SliceAccessor<T> getPageRange() {
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
      long start = nextCheckpointOffset;
      long end = start + n;

        Range<Long> claimAheadRange = Range.closedOpen(start, end);

        LockUtils.runWithLock(cache.getExecutorCreationReadLock(), () -> {
        	slice.readMetaData(metaData -> {
                RangeSet<Long> gaps = metaData.getGaps(claimAheadRange);
                processGaps(gaps, start, end);
        	});
	
	        nextCheckpointOffset = end;
        });
    }

    protected abstract void processGaps(RangeSet<Long> gaps, long start, long end);

    @Override
    protected void closeActual() {
        pageRange.close();
    }

}



//try (RefFuture<SliceMetaData> ref = slice.getMetaData()) {
//    SliceMetaData metaData = ref.await();
//    Lock readLock = metaData.getReadWriteLock().readLock();
//    readLock.lock();
//    try {
////            	Locking workerCreationLock here creates a deadlock
////            	// Prevent spawning new workers while we check whether any of the existing ones
////            	// can satisfy our demand
////            	Lock workerCreationLock = slice.getWorkerCreationLock();
////            	workerCreationLock.lock();
////            	try {
////    	if (start > 9500) {
////    		System.out.println("here");
////    	}
//            RangeSet<Long> gaps = metaData.getGaps(claimAheadRange);
//            processGaps(gaps, start, end);
////            	} finally {
////            		workerCreationLock.unlock();
////            	}
//    	
//    } finally {
//        readLock.unlock();
//    }

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

//public void checkpoint(long n) throws Exception {
//
//  long start = nextCheckpointOffset;
//  long end = start + n;
//
//  pageRange.claimByOffsetRange(start, end);
//          // Lock all pages
//  try {
//      pageRange.lock();
//
//      Deque<Range<Long>> gaps = pageRange.getGaps();
//      processGaps(gaps, start, end);
//  } finally {
//      pageRange.unlock();
//
//      nextCheckpointOffset += n;
//  }
//}
