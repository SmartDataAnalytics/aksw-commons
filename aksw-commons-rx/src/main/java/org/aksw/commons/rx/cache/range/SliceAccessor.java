package org.aksw.commons.rx.cache.range;

import java.io.IOException;
import java.util.concurrent.ConcurrentNavigableMap;

import org.aksw.commons.util.ref.RefFuture;

/**
 * Abstraction over a sequence of pages to view their content as
 * consecutive items. The underlying pages may be claimed by multiple page ranges held by different clients.
 * Clients must eventually close pages ranges in order to allow for resources to be freed.
 *
 * Note: The page range abstraction enables consumers and producers to claim the same pages independently.
 * A consumer does not have to wait for the producers to advertise pages they are working on, instead the (low-level/internal) consumer can
 * simply claim the pages it wants to read in advance and then schedule any needed executors.
 *
 * The claim range can be mutated which performs only the necessary
 * (un)claim operations.
 *
 *
 * @author raven
 *
 * @param <T>
 */
public interface SliceAccessor<A>
    extends AutoCloseable
{
    ConcurrentNavigableMap<Long, RefFuture<BufferView<A>>> getClaimedPages();


    // Allow querying the page's range that contains offset?
    // Range<Long> getEnclosingPageRange(long offset);

    /**
     * Set or update the claimed range - this will immediately request references to any pages providing the data for that range.
     * Pages outside of that range are considered as no longer needed pages will immediately be released.
     *
     * This method prepares the pages which can be subsequently locked.
     * Calling this method while the page range is locked ({@link #lock()}) raises an {@link IllegalStateException}.
     *
     * @param startOffset
     * @param endOffset
     */
    void claimByOffsetRange(long startOffset, long endOffset);

    /**
     * Lock the range for writing
     */
    void lock();

    /**
     * Put a sequence of items into the claimed range
     * Attempts to put items outside of the claimed range raises an {@link IndexOutOfBoundsException}
     *
     * The page range should be locked when calling this method.
     */
    void putAll(long offset, Object arrayWithItemsOfTypeT, int arrOffset, int arrLength);



    /**
     * Read a range of data and block if there is any missing.
     * The read operation listens on signals to the slice's hasDataCondition
     * and resumes when either the missing data became available and/or updates
     * to the slice's maximum known size indicate that there is no more data to wait for.
     *
     * @param tgt Array into which to read the data
     * @param tgtOffset Index into the tgt array
     * @param srcOffset Offset in the slice
     * @param length The maximum number of items to read
     * @return The number of items read
     */
    int blockingRead(A tgt, int tgtOffset, long srcOffset, int length) throws IOException;

    /**
     * Unlock the range
     */
    void unlock();


    /**
     * Releases all currently held pages.
     * Future requests via {@link #claimByOffsetRange(long, long)} are allowed.
     *
     */
    void releaseAll();

    /**
     * Closes the page range. Implementations should call {{@link #releaseAll()} and prevent any further
     * claims.
     */
    void close();
}

