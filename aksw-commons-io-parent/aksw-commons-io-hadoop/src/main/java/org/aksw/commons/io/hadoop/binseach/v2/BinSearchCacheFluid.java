package org.aksw.commons.io.hadoop.binseach.v2;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

class BinSearchCacheFluid
    implements BinSearchCache
{
    /* Depth at which to no switch from fixed to cache data */
    protected int fixedDepth;

    protected Cache<Long, Long> cachedDispositions;
    protected Cache<Long, HeaderRecord> cachedHeaders;

    public BinSearchCacheFluid() {
        super();
        this.fixedDepth = 16;

        cachedDispositions = Caffeine.newBuilder().maximumSize(1000000).build();
        cachedHeaders = Caffeine.newBuilder().maximumSize(1000000).build();
    }

    @Override
    public long getDisposition(long position) {
        return position;
    }

    @Override
    public void setDisposition(long from, long to) {
        cachedDispositions.put(from, to);
    }

    @Override
    public HeaderRecord getHeader(long position) {
        HeaderRecord result = cachedHeaders.getIfPresent(position);
        return result;
    }

    @Override
    public void setHeader(HeaderRecord headerRecord) {
        // HeaderRecord headerRecord = new HeaderRecord(position, displacement, header, isDataConsumed);
        cachedHeaders.put(headerRecord.position(), headerRecord);
    }
}
