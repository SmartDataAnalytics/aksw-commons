package org.aksw.commons.io.slice;

import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.util.Sync;
import org.aksw.commons.util.closeable.Disposable;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;


/**
 * A concurrently accessible sequence of data of possibly unknown size.
 *
 * @author raven
 *
 * @param <T>
 */
public interface Slice<T>
    extends SliceMetaDataBasic, Sync
{
    ReadWriteLock getReadWriteLock();
    Condition getHasDataCondition();

    ArrayOps<T> getArrayOps();

    /**
     * Protect a set of ranges from eviction.
     * If the slice does make use of eviction then this method can return null.
     * Otherwise, a disposable must be returned. As long as it is not disposed, the
     * no data in the range may get lost due to eviction.
     *
     * This method should not be used directly but via {@link SliceAccessor#addEvictionGuard(RangeSet))}.
     *
     */
    Disposable addEvictionGuard(RangeSet<Long> range);

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
     * An accessor which allows for 'claiming' a sub-range of this slice. The claimed range can be incrementally
     * modified which may re-use already allocated resources (e.g. claimed pages) and thus improve performance.
     *
     * Sub-ranges of a slice can be loaded and iterated or inserted into.
     * The sub-ranges can be modified dynamically.
     */
    SliceAccessor<T> newSliceAccessor();
}
