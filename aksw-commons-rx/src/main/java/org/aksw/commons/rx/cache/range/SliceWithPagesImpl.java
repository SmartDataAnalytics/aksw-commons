package org.aksw.commons.rx.cache.range;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.aksw.commons.store.object.key.api.KeyObjectStore;
import org.aksw.commons.util.range.BufferWithGeneration;
import org.aksw.commons.util.ref.RefFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;

public class SliceWithPagesImpl<T>
    implements SliceWithPages<T>
{
    private static final Logger logger = LoggerFactory.getLogger(SliceWithPagesImpl.class);

    protected int pageSize;
    protected AsyncClaimingCache<Long, BufferWithGeneration<T>> pageCache;
    protected AsyncClaimingCache<String, SliceMetaData> metadataCache;

    // protected ReentrantReadWriteLock executorCreationLock = new ReentrantReadWriteLock(true);
    protected ReentrantLock workerCreationLock = new ReentrantLock();

    public SliceWithPagesImpl(
            KeyObjectStore objStore,
            int pageSize,
            long maxCachedPageCount,
            Duration syncDelayDuration) {
        this.pageSize = pageSize;

        this.pageCache =
                AsyncClaimingCacheWithTransformValue.create(
                        SmartRangeCacheImpl.syncedBuffer(maxCachedPageCount, syncDelayDuration, objStore, () -> new BufferWithGeneration<T>(pageSize)),
                        Entry::getKey);

        this.metadataCache = AsyncClaimingCacheImpl.create(
                syncDelayDuration,


                   // begin of level3 setup
                   Caffeine.newBuilder()
                   //.scheduler(Scheduler.systemScheduler())
                   .maximumSize(1),
                   key -> {
                       List<String> internalKey = Arrays.asList(key);
                       SliceMetaData value;
                        try {
                            value = objStore.computeIfAbsent(internalKey, SliceMetaDataImpl::new);
                        } catch (ClassNotFoundException | IOException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                       return value;
                   },
                   (key, value, cause) -> {},
                   // end of level3 setup

                (key, value, cause) -> {
                    List<String> internalKey = Arrays.asList(key);

                    try {
                        objStore.put(internalKey, value);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    logger.info("Synced " + internalKey);
                    System.out.println("Synced" + internalKey);
                }
            );
    }



    @Override
    public long getPageSize() {
        return pageSize;
    }


    public RefFuture<SliceMetaData> getMetaData() {
        RefFuture<SliceMetaData> result = metadataCache.claim("metadata");
        return result;
    }

    @Override
    public PageRange<T> newPageRange() {
        return new PageRangeImpl<>(this);
    }

//    @Override
//    public RefFuture<RangeBuffer<T>> getPageForPageId(long pageId) {
//        RefFuture<RangeBuffer<T>> result;
//        try {
//            result = pageCache.claim(pageId);
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
//        }
//        return result;
//    }




    @Override
    public Lock getWorkerCreationLock() {
        return workerCreationLock;
    }



    @Override
    public Iterator<T> blockingIterator(long offset) {
        return new SliceWithPagesIterator<>(this, offset);
    }



    @Override
    public RefFuture<BufferWithGeneration<T>> getPageForPageId(long pageId) {
        return pageCache.claim(pageId);
    }



    @Override
    public void putAll(long offsetInBuffer, Object arrayWithItemsOfTypeT, int arrOffset, int arrLength) {
        try (PageRange<T> pageRange = newPageRange()) {
            pageRange.claimByOffsetRange(offsetInBuffer, offsetInBuffer + arrLength);

            pageRange.putAll(offsetInBuffer, arrayWithItemsOfTypeT, arrOffset, arrLength);
        }
    }

}