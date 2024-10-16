package org.aksw.commons.io.hadoop.binseach.v2;

public class BinSearchLevelCache {
    protected int fluidLevel;
    protected BinSearchCache fixedCache;
    protected BinSearchCache fluidCache;

    public BinSearchLevelCache() {
        this(16, new BinSearchCacheFixed(), new BinSearchCacheFluid());
    }

    public BinSearchLevelCache(int fluidLevel, BinSearchCache fixedCache, BinSearchCache flowCache) {
        super();
        this.fluidLevel = fluidLevel;
        this.fixedCache = fixedCache;
        this.fluidCache = flowCache;
    }

    public HeaderRecord getHeader(long position) {
        HeaderRecord result = fixedCache.getHeader(position);
        if (result == null) {
            fluidCache.getHeader(position);
        }
        return result;
    }

//    public void setHeader(int depth, long position, int displacement, byte[] header, boolean isDataConsumed) {
//        BinSearchCache target = depth < flowLevel ? fixedCache : fluidCache;
//        target.setHeader(position, displacement, header, isDataConsumed);
//    }

    public void setHeader(int depth, HeaderRecord headerRecord) {
        BinSearchCache target = depth < fluidLevel ? fixedCache : fluidCache;
        target.setHeader(headerRecord);
    }

    public long getDisposition(long position) {
        long result = fixedCache.getDisposition(position);
        if (result < 0) {
            fluidCache.getDisposition(position);
        }
        return result;
    }

    public void setDisposition(int depth, long from, long to) {
        BinSearchCache target = depth < fluidLevel ? fixedCache : fluidCache;
        target.setDisposition(from, to);
    }

    public static BinSearchLevelCache noCache() {
        return new BinSearchLevelCache(0, new BinarySearchCacheNoop(), new BinarySearchCacheNoop());
    }

    public static BinSearchLevelCache dftCache() {
        return new BinSearchLevelCache();
    }
}
