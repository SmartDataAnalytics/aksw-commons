package org.aksw.commons.io.input;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;

public class DataStreams {
    public static ReadableByteChannel newChannel(DataStream<byte[]> dataStream) {
        return new ReadableByteChannelOverDataStream(dataStream);
    }

    public static InputStream newInputStream(DataStream<byte[]> dataStream) {
        return Channels.newInputStream(newChannel(dataStream));
    }

    public static <T> Iterator<T> newIterator(DataStream<T[]> dataStream) {
        return new IteratorOverDataStream<>(dataStream.getArrayOps(), dataStream);
    }
}
