package org.aksw.commons.util.array;

/**
 * BufferLike is a common interface for Buffer and RangeBuffer.
 * Even though both specializations have most methods in common, the semantics differ in subtle ways:
 * A range buffer may only allow reading within valid ranges, as such it may not be valid to read a range
 * from 0 to the buffer's capacity.
 */
public interface BufferLike<A>
    extends ArrayWritable<A>, ArrayReadable<A>, HasArrayOps<A>
{
    ArrayOps<A> getArrayOps();


    /** Buffers with 'unlimited' capacity should return Long.MAX_VALUE */
    long getCapacity();

    BufferLike<A> slice(long offset, long length);
}
