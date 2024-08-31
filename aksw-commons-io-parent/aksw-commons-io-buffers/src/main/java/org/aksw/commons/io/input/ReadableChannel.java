package org.aksw.commons.io.input;

import java.nio.channels.Channel;

/**
 * A data stream allows for repeated retrieval of arrays of consecutive items.
 * Data streams can be seen as a low level generalizaton / unification of Iterators and InputStreams.
 * See {@link ReadableChannels#newIterator(ReadableChannel)} and {@link ReadableChannels#newInputStream(ReadableChannel)}.
 *
 * Akin to an InputStream, the {@link ReadableChannel} interface does not provide a seek() method.
 * Usually there should be another factory that creates data streams
 * for given offsets. The reason is, that a sequential reader is typically backed by a stream of items
 * (such as a http response, or a sql/sparql result set) and that stream needs to be re-created when
 * jumping to arbitrary offsets.
 *
 *
 * @author Claus Stadler, Feb 17, 2022
 *
 * @param <A>
 */
public interface ReadableChannel<A>
    extends ReadableSource<A>, Channel
{


    /** Returns a stream over the elements of the channel.
     * Closing the stream closes the channel. */
    /*
    @SuppressWarnings({ "unchecked", "rawtypes" })
    default <T> Stream<T> asStream() {
        return ReadableChannels.newStream((ReadableChannel)this);
    }
    */
}
