package org.aksw.commons.io.input;

import java.io.IOException;


/**
 * A factory for ReadableChannel instances.
 * The object is akin to a JDBC datasource; the created channels correspond to JDBC connections.
 */
public interface ReadableChannelSource<A>
    extends ReadableChannelFactory<A>
{
    /**
     * ReadableChannelSources are implemented against the newRedableChannel(Range) method.
     * Needs consolidation.
     */
    @Override
    default ReadableChannel<A> newReadableChannel(long start, long end) throws IOException {
        throw new UnsupportedOperationException();
    }

    /** The size; -1 if unknown */
    long size() throws IOException;
}
