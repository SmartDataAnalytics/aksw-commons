package org.aksw.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.aksw.commons.collections.CloseableIterator;
import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.buffer.array.ArrayReadable;
import org.aksw.commons.io.buffer.plain.BufferOverArray;
import org.aksw.commons.io.util.channel.ReadableByteChannelWithoutCloseOnInterrupt;

import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;
import com.google.common.primitives.Ints;

public class ReadableChannels {
    public static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    public static <A> SeekableReadableChannel<A> shiftOffset(SeekableReadableChannel<A> dataStream, long offset) {
        return new SeekableReadableChannelWithOffset<>(dataStream, offset);
    }

    public static <A> ReadableChannel<A> limit(ReadableChannel<A> dataStream, long limit) {
        return new ReadableChannelWithLimit<>(dataStream, limit);
    }

    public static <A> ReadableChannel<A> empty(ArrayOps<A> arrayOps) {
        return newChannel(BufferOverArray.create(arrayOps, 0), 0);
    }

    public static <A> ReadableChannel<A> of(ArrayOps<A> arrayOps, A array) {
        return newChannel(BufferOverArray.create(arrayOps, array), 0);
    }

    public static <A> ReadableChannel<A> of(ArrayOps<A> arrayOps, A array, int pos) {
        return newChannel(BufferOverArray.create(arrayOps, array), pos);
    }

    public static ReadableChannel<byte[]> wrap(ReadableByteChannel channel) {
        return new ReadableChannelOverReadableByteChannel(channel);
    }

    /** Note: The inputStream is internally wrapped with custom readable byte channel
     * that does not close the input stream when a thread gets interrupted.
     * This is essential when probing a seekable input stream for record offsets.
     * Channels.newChannel does close streams on interrupt.
     */
    public static ReadableChannel<byte[]> wrap(InputStream inputStream) {
        return wrap(new ReadableByteChannelWithoutCloseOnInterrupt(inputStream));
        // return wrap(Channels.newChannel(inputStream));
    }

    public static <T> ReadableChannel<T[]> wrap(Stream<T> stream, ArrayOps<T[]> arrayOps) {
        return new ReadableChannelOverIterator<>(arrayOps, stream.iterator(), stream::close);
    }

    public static <A> SeekableReadableChannelOverBuffer<A> newChannel(ArrayReadable<A> arrayReadable) {
        return newChannel(arrayReadable, 0);
    }

    public static <A> SeekableReadableChannelOverBuffer<A> newChannel(ArrayReadable<A> arrayReadable, long pos) {
        return new SeekableReadableChannelOverBuffer<>(arrayReadable, pos);
    }

    public static ReadableByteChannel newChannel(ReadableChannel<byte[]> dataStream) {
        return new ReadableByteChannelOverDataStream(dataStream);
    }

    public static InputStream newInputStream(ReadableChannel<byte[]> dataStream) {
        return Channels.newInputStream(newChannel(dataStream));
    }

    public static <T> CloseableIterator<T> newIterator(ReadableChannel<T[]> dataStream) {
        return newIterator(dataStream, DEFAULT_BUFFER_SIZE);
    }

    public static <T> CloseableIterator<T> newIterator(ReadableChannel<T[]> dataStream, int internalBufferSize) {
        return new IteratorOverReadableChannel<>(dataStream.getArrayOps(), dataStream, internalBufferSize);
    }

    /** Wrap as a java8 stream. Closing the returned stream also closes the dataStream. */
    public static <T> Stream<T> newStream(ReadableChannel<T[]> dataStream) {
        return Streams.stream(newIterator(dataStream)).onClose(() -> {
            try {
                dataStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <T> Iterator<T> newBoxedIterator(ReadableChannel<?> dataStream) {
        return newBoxedIterator(dataStream, DEFAULT_BUFFER_SIZE);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> CloseableIterator<T> newBoxedIterator(ReadableChannel<?> dataStream, int internalBufferSize) {
        return new IteratorOverReadableChannel(dataStream.getArrayOps(), dataStream, internalBufferSize);
    }

    public static <T> Stream<T> newBoxedStream(ReadableChannel<?> dataStream) {
        return newBoxedStream(dataStream, DEFAULT_BUFFER_SIZE);
    }

    public static <T> Stream<T> newBoxedStream(ReadableChannel<?> dataStream, int internalBufferSize) {
        return Streams.<T>stream(newBoxedIterator(dataStream, internalBufferSize)).onClose(() -> {
            try {
                dataStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** Returns a char sequence over the given channel where the current position in the channel
     * corresponds to byte 0 */
    public static CharSequence asCharSequence(SeekableReadableChannel<byte[]> channel) {
        Objects.requireNonNull(channel);
        long pos = channel.position();
        SeekableReadableChannel<byte[]> shifted = shiftOffset(channel, pos);
        return asCharSequence(shifted, Integer.MAX_VALUE);
    }

    /** Ensure that the length is NOT greater than the amount of available data! */
    public static CharSequence asCharSequence(SeekableReadableChannel<byte[]> channel, int length) {
        return new CharSequenceOverSeekableReadableChannelOfBytes(channel, length);
    }


    public static <A, X extends ReadableChannel<A>> ReadableChannelWithCounter<A, X> withCounter(X decoratee) {
        return new ReadableChannelWithCounter<>(decoratee);
    }

    public static <A, T, X extends ReadableChannel<A>> ReadableChannelWithValue<A, T, X> withValue(X decoratee, T value) {
        return new ReadableChannelWithValue<>(decoratee, value);
    }

    public static <A> ReadableChannel<A> concat(List<ReadableChannel<A>> channels) {
        Preconditions.checkArgument(!channels.isEmpty());
        return concat(channels.get(0).getArrayOps(), channels);
    }

    public static <A> ReadableChannel<A> concat(ArrayOps<A> arrayOps, List<ReadableChannel<A>> channels) {
        return new ReadableChannelConcat<>(arrayOps, channels);
    }

    public static <A> long skip(ReadableChannel<A> channel, long req, A array, int position, int length) throws IOException {
        if (length == 0) {
            // TODO Create a temp buffer when that happens
            throw new IllegalStateException("Must not be called with length 0");
        }

        long result = 0;
        while (result < req) {
            int l = Ints.saturatedCast(Math.min(req - result, length));
            int n = channel.read(array, position, l);
            if (n < 0) {
                break;
            }
            result += n;
        }

        return result;
    }

    public static <A> int readFully(ReadableChannel<A> channel, A array, int position, int length) throws IOException {
    	int result = 0;
    	int l = length;
    	int p = position;
    	while (l > 0) {
    		int n = channel.read(array, p, l);
    		if (n < 0) {
    			break;
    		}
    		result += n;
    		l -= n;
    		p += n;
    	}
    	return result;
    }
    
    public static <T> ReadableChannel<T> closeShield(ReadableChannel<T> in) {
        Objects.requireNonNull(in);
        return new ReadableChannelDecoratorBase<>(in) {
        	@Override
        	public void close() throws IOException {
        		// No op / close shield
        	}
        };
    }
}
