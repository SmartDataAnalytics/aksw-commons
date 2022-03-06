package org.aksw.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;
import java.util.stream.Stream;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.buffer.plain.BufferOverArray;

import com.google.common.collect.Streams;

public class DataStreams {
    public static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    public static <A> DataStream<A> limit(DataStream<A> dataStream, long limit) {
        return new DataStreamLimit<>(dataStream, limit);
    }

    public static <A> DataStream<A> empty(ArrayOps<A> arrayOps) {
        return new DataStreamOverBuffer<>(BufferOverArray.create(arrayOps, 0), 0);
    }

    public static <A> DataStream<A> of(ArrayOps<A> arrayOps, A array) {
        return new DataStreamOverBuffer<>(BufferOverArray.create(arrayOps, array), 0);
    }

    public static DataStream<byte[]> wrap(ReadableByteChannel channel) {
        return new DataStreamOverReadableByteChannel(channel);
    }

    public static ReadableByteChannel newChannel(DataStream<byte[]> dataStream) {
        return new ReadableByteChannelOverDataStream(dataStream);
    }

    public static InputStream newInputStream(DataStream<byte[]> dataStream) {
        return Channels.newInputStream(newChannel(dataStream));
    }

    public static <T> Iterator<T> newIterator(DataStream<T[]> dataStream) {
        return newIterator(dataStream, DEFAULT_BUFFER_SIZE);
    }

    public static <T> Iterator<T> newIterator(DataStream<T[]> dataStream, int internalBufferSize) {
        return new IteratorOverDataStream<>(dataStream.getArrayOps(), dataStream, internalBufferSize);
    }

    /** Wrap as a java8 stream. Closing the returned stream also closes the dataStream. */
    public static <T> Stream<T> newStream(DataStream<T[]> dataStream) {
        return Streams.stream(newIterator(dataStream)).onClose(() -> {
            try {
                dataStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <T> Iterator<T> newBoxedIterator(DataStream<?> dataStream) {
        return newBoxedIterator(dataStream, DEFAULT_BUFFER_SIZE);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> Iterator<T> newBoxedIterator(DataStream<?> dataStream, int internalBufferSize) {
        return new IteratorOverDataStream(dataStream.getArrayOps(), dataStream, internalBufferSize);
    }

    public static <T> Stream<T> newBoxedStream(DataStream<?> dataStream) {
        return newBoxedStream(dataStream, DEFAULT_BUFFER_SIZE);
    }

    public static <T> Stream<T> newBoxedStream(DataStream<?> dataStream, int internalBufferSize) {
        return Streams.<T>stream(newBoxedIterator(dataStream, internalBufferSize)).onClose(() -> {
            try {
                dataStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
