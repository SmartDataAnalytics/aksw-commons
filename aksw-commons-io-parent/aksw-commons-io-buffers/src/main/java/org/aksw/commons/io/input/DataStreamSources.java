package org.aksw.commons.io.input;

import java.util.function.Consumer;

import org.aksw.commons.io.cache.AdvancedRangeCacheConfig;
import org.aksw.commons.io.cache.AdvancedRangeCacheImpl;
import org.aksw.commons.io.slice.Slice;
import org.aksw.commons.io.slice.SliceWithPagesSyncToDisk;
import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathOpsStr;
import org.aksw.commons.store.object.key.api.ObjectStore;
import org.aksw.commons.store.object.key.impl.KryoUtils;
import org.aksw.commons.store.object.key.impl.ObjectStoreImpl;
import org.aksw.commons.store.object.path.impl.ObjectSerializerKryo;

import com.esotericsoftware.kryo.Kryo;

public class DataStreamSources {

    public static DataStreamSource<byte[]> of(java.nio.file.Path path) {
        return new DataStreamSourceOverPath(path);
    }

    public static <A> DataStreamSource<A> cache(
            DataStreamSource<A> source,
            java.nio.file.Path cacheBaseFolder,
            String cacheEntryId,
            AdvancedRangeCacheConfig cacheConfig) {
        Path<String> relPath = PathOpsStr.create(cacheEntryId);
        if (relPath.isAbsolute()) {
            throw new IllegalArgumentException("Cache entries must map to relative paths");
        }

        return cache(source, cacheBaseFolder, relPath, cacheConfig);
    }

    public static <A> DataStreamSource<A> cache(
            DataStreamSource<A> source,
            java.nio.file.Path cacheBaseFolder,
            Path<String> cacheEntryId,
            AdvancedRangeCacheConfig cacheConfig) {
        return cache(source, cacheBaseFolder, cacheEntryId, cacheConfig, null);
    }

    public static <A> DataStreamSource<A> cache(
            DataStreamSource<A> source,
            java.nio.file.Path cacheBaseFolder,
            Path<String> cacheEntryId,
            AdvancedRangeCacheConfig cacheConfig,
            Consumer<Kryo> customRegistrator) {

        ObjectStore objectStore = ObjectStoreImpl.create(cacheBaseFolder, ObjectSerializerKryo.create(
                KryoUtils.createKryoPool(customRegistrator)));

        return cache(source, objectStore, cacheEntryId, cacheConfig);
    }

    public static <A> DataStreamSource<A> cache(
            DataStreamSource<A> source,
            ObjectStore objectStore,
            Path<String> cacheEntryId,
            AdvancedRangeCacheConfig cacheConfig) {

        SliceWithPagesSyncToDisk<A> slice = SliceWithPagesSyncToDisk.create(
                source.getArrayOps(), objectStore, cacheEntryId,
                cacheConfig.getPageSize(), cacheConfig.getTerminationDelay());

        return cache(source, slice, cacheConfig);
    }

    public static <A> DataStreamSource<A> cache(
            DataStreamSource<A> source,
            Slice<A> slice,
            AdvancedRangeCacheConfig cacheConfig) {

        AdvancedRangeCacheImpl<A> result = AdvancedRangeCacheImpl.<A>newBuilder()
                .setDataSource(source)
                .setWorkerBulkSize(cacheConfig.getInternalWorkerSize())
                .setSlice(slice)
                .setRequestLimit(cacheConfig.getMaxRequestSize())
                .setTerminationDelay(cacheConfig.getTerminationDelay())
                .build();

        return result;
    }

}
