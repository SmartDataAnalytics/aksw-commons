package org.aksw.commons.io.input;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.HasArrayOps;

public interface ReadableSource<A>
    extends HasArrayOps<A>
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
