package org.aksw.commons.rx.cache.slice;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aksw.commons.collection.slice.SliceMetaData;
import org.aksw.commons.lock.LockUtils;

import com.google.common.collect.Range;


/**
 * A concurrently accessible sequence of data of possibly unknown size.
 *
 * The interface is meant to support auto-syncing as follows:
 * References to metadata and content can be made.
 * Once all references are released a sync-task can be scheduled the persists
 * the updated state. Implementations may delay syncing if within a certain time
 * window another content/metadata block is referenced.
 *
 *
 * @author raven
 *
 * @param <T>
 */
public interface Slice<A> {
    /**
     * Obtain a new reference to the metadata. The referent may be loaded lazily.
     * The reference must be closed after use in order to allow sync to trigger.
     * 
     * @return
     */
    SliceMetaData getMetaData();

    /**
     * Sub-ranges of a slice can be loaded and iterated or inserted into.
     * The sub-ranges can be modified dynamically.
     */
    SliceAccessor<A> newSliceAccessor();
    

    /**
     * Read the metadata and check whether the slice has a known size and
     * there is only a single range of loaded data starting from offset 0 to that size.
     * 
     * @return
     */
    default boolean isComplete() {
        boolean result = computeFromMetaData(false, metaData -> {
            long knownSize = metaData.getKnownSize();
            Set<Range<Long>> ranges = metaData.getLoadedRanges().asRanges();

            Range<Long> range = ranges.size() == 1 ? ranges.iterator().next() : null;

            long endpoint = range != null ? range.upperEndpoint() : -1;

            boolean r = knownSize >= 0 && endpoint >= 0 && knownSize == endpoint;
            return r;
        });

        return result;
    }

    default void mutateMetaData(Consumer<? super SliceMetaData> fn) {
        computeFromMetaData(true, metaData -> { fn.accept(metaData); return null; });
    }

    default void readMetaData(Consumer<? super SliceMetaData> fn) {
        computeFromMetaData(false, metaData -> { fn.accept(metaData); return null; });
    }


    /**
     * Lock the metadata and then invoke a value returning function on it.
     * Afterwards release the lock. Returns the obtained value. 
     * 
     * @param <X>
     * @param isWrite
     * @param fn
     * @return
     */
    default <X> X computeFromMetaData(boolean isWrite, Function<? super SliceMetaData, X> fn) {
        SliceMetaData metaData = getMetaData();
        ReadWriteLock rwl = metaData.getReadWriteLock();
        Lock lock = isWrite ? rwl.writeLock() : rwl.readLock();
        X result = LockUtils.runWithLock(lock, () -> {
            X r = fn.apply(metaData);

            if (isWrite) {
                metaData.getHasDataCondition().signalAll();
            }
            return r;
        });

        return result;
    }

}
