package org.aksw.commons.io.input;

import java.io.IOException;
import java.nio.channels.Channel;

import org.aksw.commons.io.buffer.array.HasArrayOps;

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
    extends HasArrayOps<A>, Channel
{

    /**
     * Read method following the usual InputStream protocol.
     *
     * @param array The array into which to put the read data
     * @param position Offset into array where to start writing
     * @param length Maximum number of items to read.
     * @return The number of items read. Return -1 if end of data was reached, and 0 iff length was 0.
     *
     * @throws IOException
     */
    int read(A array, int position, int length) throws IOException;

    @SuppressWarnings("unchecked")
    default int readRaw(Object array, int position, int length) throws IOException {
        return read((A)array, position, length);
    }
}
