package org.aksw.commons.rx.cache.range;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aksw.commons.util.ref.RefFuture;

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
public interface SliceWithAutoSync<T>
    extends Puttable
{
    /**
     * Obtain a new reference to the metadata.
     *
     * The reference must be closed after use in order to allow sync to trigger
     * @return
     */
    RefFuture<SliceMetaData> getMetaData();


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


    default <X> X computeFromMetaData(boolean isWrite, Function<? super SliceMetaData, X> fn) {
        X result;
        try (RefFuture<SliceMetaData> ref = getMetaData()) {
            SliceMetaData metaData = ref.await();
            ReadWriteLock rwl = metaData.getReadWriteLock();
            Lock lock = isWrite ? rwl.writeLock() : rwl.readLock();
            lock.lock();
            try {
                result = fn.apply(metaData);

                if (isWrite) {
                    metaData.getHasDataCondition().signalAll();
                }
            } finally {
                lock.unlock();
            }
        }

        return result;
    }

    /**
     * Return an iterator initialized at the given offset
     * which blocks upon accessing an index outside of the data or failure ranges.
     *
     * @param offset
     * @return
     */
    Iterator<T> blockingIterator(long offset);

    // void syncMetaData();
    // syncPages();
    // sync(); // Sync everything

    /**
     * Sub-ranges of a slice can be loaded and iterated or inserted into.
     * The sub-ranges can be modified dynamically.
     */
    PageRange<T> newPageRange();

    /**
     * A lock that when held prevents creation of workers that put data into the slice.
     * This allows for analyzing all existing workers when having to decide whether
     * a new worker needs to be created.
     *
     *
     * Note: This might not be the best place for the lock because it
     * seems better to have that lock on a data producer system (e.g. the SmartRangeCache impl).
     * The slice itself is no data producer but rather a data collection.
     *
     *
     */
    Lock getWorkerCreationLock();
}
