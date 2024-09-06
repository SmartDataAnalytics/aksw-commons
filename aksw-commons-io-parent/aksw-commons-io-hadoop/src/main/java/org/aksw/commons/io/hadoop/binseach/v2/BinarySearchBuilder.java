package org.aksw.commons.io.hadoop.binseach.v2;

import java.nio.file.Path;
import java.util.function.Supplier;

import org.aksw.commons.io.binseach.BinarySearcher;
import org.aksw.commons.io.hadoop.binseach.v2.BinSearchResourceCache.CacheEntry;
import org.aksw.commons.io.input.SeekableReadableChannelSource;
import org.apache.hadoop.io.compress.SplittableCompressionCodec;

import com.github.benmanes.caffeine.cache.Caffeine;

public class BinarySearchBuilder {
    protected Path path;
    protected SplittableCompressionCodec codec;
    // protected Cache<Long, Block> blockCache;
    //protected BinSearchLevelCache binSearchCache;
    protected BinSearchResourceCache resourceCache;

    public static BinarySearchBuilder newBuilder() {
        return new BinarySearchBuilder();
    }

    public BinarySearchBuilder setSource(Path path) {
        this.path = path;
        return this;
    }

    public BinarySearchBuilder setCodec(SplittableCompressionCodec codec) {
        this.codec = codec;
        return this;
    }

    public BinarySearchBuilder setResourceCache(BinSearchResourceCache resourceCache) {
        this.resourceCache = resourceCache;
        return this;
    }


//    public BinarySearchBuilder setBlockCache(Cache<Long, Block> blockCache) {
//        this.blockCache = blockCache;
//        return this;
//    }
//
//    public BinarySearchBuilder setBlockCacheSize(int maxSize) {
//        this.blockCache = Caffeine.newBuilder().maximumSize(maxSize).build();
//        return this;
//    }
//
//    public BinarySearchBuilder setBinSearchCache(BinSearchLevelCache binSearchCache) {
//        this.binSearchCache = binSearchCache;
//        return this;
//    }

    public BinarySearcher build() {
        BinarySearcher result;

        Supplier<CacheEntry> cacheSupplier = resourceCache != null
                ? () -> resourceCache.getOrCreate(path)
                : () -> new CacheEntry(BinSearchLevelCache.dftCache(), Caffeine.newBuilder().maximumSize(16).build());

//        BinSearchLevelCache finalBinSearchCache = binSearchCache != null
//                ? binSearchCache
//                : BinSearchLevelCache.dftCache();

        if (codec == null) {
            SeekableReadableChannelSource<byte[]> source = new SeekableReadableChannelSourceOverNio(path);
            result = new BinarySearcherOverPlainSource(source, cacheSupplier);
        } else {
            BlockSource blockSource = BlockSource.of(path, codec);

//            Cache<Long, Block> finalBlockCache = blockCache != null
//                    ? blockCache
//                    : Caffeine.newBuilder().maximumSize(16).build();

            result = new BinarySearcherOverBlockSource(blockSource, cacheSupplier);
        }
        return result;
    }
}
