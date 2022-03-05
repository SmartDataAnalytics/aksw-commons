package org.aksw.commons.io.buffer.array;

/**
 * BufferLike is a common interface for Buffer and RangeBuffer.
 * Even though both specializations have most methods in common, the semantics differ in subtle ways:
 * A buffer must support reading any slice of data within its capacity.
 * A range buffer only allows for reading within valid ranges and raises an exception upon violation.
 */
public interface BufferLike<A>
    extends ArrayWritable<A>, ArrayReadable<A>, HasArrayOps<A>
{
    ArrayOps<A> getArrayOps();

    /** Buffers with 'unlimited' capacity should return Long.MAX_VALUE */
    long getCapacity();

    BufferLike<A> slice(long offset, long length);
}
