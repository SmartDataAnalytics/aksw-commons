package org.aksw.commons.io.slice;

import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;

import org.aksw.commons.cache.async.AsyncClaimingCache;
import org.aksw.commons.cache.async.AsyncClaimingCacheImpl;
import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.buffer.plain.Buffer;
import org.aksw.commons.io.buffer.plain.BufferOverArray;
import org.aksw.commons.io.buffer.range.RangeBuffer;
import org.aksw.commons.io.buffer.range.RangeBufferImpl;
import org.aksw.commons.util.lock.LockUtils;
import org.aksw.commons.util.ref.RefFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Range;

/**
 * A slice implementation that starts to discard pages once there are too many.
 *
 * @author raven
 *
 */
public class SliceInMemoryCache<A>
    extends SliceBase<A>
    implements SliceWithPages<A>
{
    private static final Logger logger = LoggerFactory.getLogger(SliceInMemoryCache.class);

    protected SliceMetaDataWithPages metaData;
    protected AsyncClaimingCache<Long, BufferView<A>> pageCache;

    protected SliceInMemoryCache(ArrayOps<A> arrayOps, int pageSize, AsyncClaimingCacheImpl.Builder<Long, BufferView<A>> cacheBuilder) {
        super(arrayOps);
        this.metaData = new SliceMetaDataWithPagesImpl(pageSize);
        this.pageCache = cacheBuilder
                .setCacheLoader(this::loadPage)
                .setEvictionListener((k, v, c) -> evicePage(k))
                .build();
    }

    public static <A> Slice<A> create(ArrayOps<A> arrayOps, int pageSize, int maximumSize) {
        AsyncClaimingCacheImpl.Builder<Long, BufferView<A>> cacheBuilder = AsyncClaimingCacheImpl.newBuilder(
            Caffeine.newBuilder().maximumSize(maximumSize));

        return new SliceInMemoryCache<>(arrayOps, pageSize, cacheBuilder);
    }

    protected void evicePage(long pageId) {
        long pageOffset = getPageOffsetForPageId(pageId);
        int pageSize = metaData.getPageSize();

        Range<Long> pageRange = Range.closedOpen(pageOffset, pageOffset + pageSize);
        LockUtils.runWithLock(readWriteLock.writeLock(), () -> {
            metaData.getLoadedRanges().remove(pageRange);

            if (logger.isDebugEnabled()) {
                logger.debug("Eviced page " + pageId + " with range " + pageRange);
            }
        });
    }

    protected BufferView<A> loadPage(long pageId) {
        long pageOffset = getPageOffsetForPageId(pageId);

        Buffer<A> buffer = BufferOverArray.create(arrayOps, metaData.getPageSize());
        RangeBuffer<A> rangeBuffer = RangeBufferImpl.create(metaData.getLoadedRanges(), pageOffset, buffer);

        return new BufferView<A>() {
            @Override
            public RangeBuffer<A> getRangeBuffer() {
                return rangeBuffer;
            }

            @Override
            public long getGeneration() {
                return 0;
            }

            @Override
            public ReadWriteLock getReadWriteLock() {
                return readWriteLock;
            }
        };
    }

    public static <A> SliceInMemory<A> create(ArrayOps<A> arrayOps, Buffer<A> buffer) {
        return new SliceInMemory<>(arrayOps, buffer);
    }

    @Override
    protected SliceMetaDataBasic getMetaData() {
        return metaData;
    }

    @Override
    public void sync() throws IOException {
    }

    @Override
    public long getPageSize() {
        return metaData.getPageSize();
    }

    @Override
    public RefFuture<BufferView<A>> getPageForPageId(long pageId) {
        return pageCache.claim(pageId);
    }
}
