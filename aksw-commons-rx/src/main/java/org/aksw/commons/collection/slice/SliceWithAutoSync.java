package org.aksw.commons.collection.slice;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aksw.commons.util.array.ArrayOps;

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
    extends SliceMetaDataBasic
    // extends ArrayPuttable
{
    ReadWriteLock getReadWriteLock();
    Condition getHasDataCondition();

    ArrayOps<T> getArrayOps();
    // void checkForUpdate();

    /**
     * Obtain a new reference to the metadata. The referent may be loaded lazily.
     * The reference must be closed after use in order to allow sync to trigger.
     *
     * @return
     */
    // RefFuture<SliceMetaData> getMetaData();


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

    default void mutateMetaData(Consumer<? super SliceMetaDataBasic> fn) {
        computeFromMetaData(true, metaData -> { fn.accept(metaData); return null; });
    }

    default void readMetaData(Consumer<? super SliceMetaDataBasic> fn) {
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
    default <X> X computeFromMetaData(boolean isWrite, Function<? super SliceMetaDataBasic, X> fn) {
        X result;
        ReadWriteLock rwl = this.getReadWriteLock();
        Lock lock = isWrite ? rwl.writeLock() : rwl.readLock();
        lock.lock();
        try {
            result = fn.apply(this);

            if (isWrite) {
                this.getHasDataCondition().signalAll();
            }
        } finally {
            lock.unlock();
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
    // Iterator<T> blockingIterator(long offset);

    // void syncMetaData();
    // syncPages();
    // sync(); // Sync everything
    void sync() throws IOException;

    /**
     * An accessor which allows for 'claiming' a sub-range of this slice. The claimed range can be incrementally
     * modified which may re-use already allocated resources (e.g. claimed pages) and thus improve performance.
     *
     * Sub-ranges of a slice can be loaded and iterated or inserted into.
     * The sub-ranges can be modified dynamically.
     */
    SliceAccessor<T> newSliceAccessor();

    /**
     * A lock that when held prevents creation of workers that put data into the slice.
     * This allows for analyzing all existing workers during scheduling; i.e. when deciding
     * whether for a data demand any new workers need to be created or existing ones can be reused.
     *
     *
     * Note: This might not be the best place for the lock because it
     * seems better to have that lock on a data producer system (e.g. the SmartRangeCache impl).
     * The slice itself is no data producer but rather a data collection.
     *
     *
     */
    // Lock getWorkerCreationLock();
}
