package org.aksw.commons.io.binseach;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.aksw.commons.io.input.ReadableChannelSupplier;
import org.aksw.commons.io.input.ReadableChannels;

public interface BinarySearcher
    extends AutoCloseable
{
    InputStream search(byte[] prefix) throws IOException;

    // Add default method for CharSequence?

    default InputStream search(String prefixStr) throws IOException {
        InputStream result = search(prefixStr == null ? null : prefixStr.getBytes());
        return result;
    }

    // XXX Not ideal mixing InputStream and Channel
    default Stream<ReadableChannelSupplier<byte[]>> parallelSearch(byte[] prefix) throws IOException {
        return Stream.of(() -> {
            try {
                return ReadableChannels.wrap(search(prefix));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    void close() throws Exception;
}
