package org.aksw.commons.rx.cache.range;

/**
 * Abstraction over a sequence of pages to view their content as
 * consecutive items.
 * The claim range can be mutated which performs only the necessary
 * (un)claim operations.
 *
 *
 * @author raven
 *
 * @param <T>
 */
public interface PageRange<T>
    extends AutoCloseable
{
//     ConcurrentNavigableMap<Long, RefFuture<RangeBuffer<T>>> getClaimedPages();


    // Allow querying the page's range that contains offset?
    // Range<Long> getEnclosingPageRange(long offset);

    void claimByOffsetRange(long startOffset, long endOffset);

    void lock();

    /**
     * Put a sequence of items into the claimed range
     * Attempts to put items outside of the claimed range raises an {@link IndexOutOfBoundsException}
     *
     */
    void putAll(long offset, Object arrayWithItemsOfTypeT, int arrOffset, int arrLength);

    // Deque<Range<Long>> getGaps();

    void unlock();

    void releaseAll();

    // TODO Implement close such that further claims are prevented
    void close();
}

