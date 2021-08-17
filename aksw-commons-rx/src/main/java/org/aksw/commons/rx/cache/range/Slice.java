package org.aksw.commons.rx.cache.range;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;

import org.aksw.commons.util.ref.RefFuture;


/**
 * A sequence of data with possibly unknown exact size.
 *
 * @author raven
 *
 * @param <T>
 */
public interface Slice<T>
    extends PutHelper
{
    /**
     * Obtain a new reference to the metadata.
     * The reference must be closed after use in order
     * to allow sync to trigger
     * @return
     */
    RefFuture<SliceMetaData> getMetaData();


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
     * This might not be the best place for the lock because it
     * seems better to have that lock on a data producer system (e.g. the SmartRangeCache impl).
     * The slice itself is no data producer but rather a data collection.
     *
     *
     */
    Lock getWorkerCreationLock();
}
