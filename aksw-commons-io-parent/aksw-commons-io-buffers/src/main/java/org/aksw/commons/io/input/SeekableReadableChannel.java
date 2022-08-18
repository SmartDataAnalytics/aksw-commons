package org.aksw.commons.io.input;

public interface SeekableReadableChannel<A>
    extends ReadableChannel<A>, HasPosition, Cloneable
{
    /**
     * Optional operation.
     * Open a new channel to the underlying source at the same position as this channel.
     * The returned channel is an independent entity and needs to be closed separatedly.
     * Opening a channel this way may be faster than opening a new channel at the source
     * because information about this channel may be re-used
     * (e.g. held pages and pointers into internal data structures)
     */
    //@Override
    // SeekableReadableChannel<A> clone() throws CloneNotSupportedException;

    SeekableReadableChannel<A> cloneObject(); // throws CloneNotSupportedException;
}
