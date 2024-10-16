package org.aksw.commons.io.input;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.buffer.array.ArrayReadable;
import org.aksw.commons.io.buffer.plain.BufferOverArray;

public class SeekableReadableChannels {

    public static <A, X extends SeekableReadableChannel<A>> SeekableReadableChannel<A> omitBlockMarker(X delegate) {
        return new SeekableReadableChannelOmitBlockMarker<A, SeekableReadableChannel<A>>(delegate);
    }

    public static <A> SeekableReadableChannel<A> shiftOffset(SeekableReadableChannel<A> dataStream, long offset) {
        return new SeekableReadableChannelWithOffset<>(dataStream, offset);
    }

    public static SeekableReadableChannel<byte[]> wrap(SeekableByteChannel nioChannel) {
        return new SeekableReadableChannelOverNio<>(nioChannel);
    }

    public static SeekableByteChannel adapt(SeekableReadableChannel<byte[]> channel) {
        return new SeekableByteChannelAdapter<>(channel);
    }

    public static <A> SeekableReadableChannelOverBuffer<A> newChannel(ArrayReadable<A> arrayReadable) {
        return newChannel(arrayReadable, 0);
    }

    public static <A> SeekableReadableChannelOverBuffer<A> newChannel(ArrayReadable<A> arrayReadable, long pos) {
        return new SeekableReadableChannelOverBuffer<>(arrayReadable, pos);
    }

    public static <A> SeekableReadableChannelOverBuffer<A> empty(ArrayOps<A> arrayOps) {
        return newChannel(BufferOverArray.create(arrayOps, 0), 0);
    }

    public static <A> SeekableReadableChannelOverBuffer<A> of(ArrayOps<A> arrayOps, A array) {
        return newChannel(BufferOverArray.create(arrayOps, array), 0);
    }

    public static <A> SeekableReadableChannelOverBuffer<A> of(ArrayOps<A> arrayOps, A array, int pos) {
        return newChannel(BufferOverArray.create(arrayOps, array), pos);
    }

    /** Wrap a readable channel such the positioning via skipping becomes possible */
    public static <A> SeekableReadableChannel<A> wrapForwardSeekable(ReadableChannel<A> channel, long basePos) {
        return new SeekableReadableChannelOverReadableChannel<>(channel, basePos);
    }

    public static <T> SeekableReadableChannel<T> closeShield(SeekableReadableChannel<T> in) {
        Objects.requireNonNull(in);
        return new SeekableReadableChannelDecoratorBase<>(in) {
            @Override
            public void close() throws IOException {
                // No op / close shield
            }
        };
    }

    /** Returns a char sequence over the given channel where the current position in the channel
     * corresponds to byte 0 */
    public static CharSequence asCharSequence(SeekableReadableChannel<byte[]> channel) {
        Objects.requireNonNull(channel);
        long pos;
        try {
            pos = channel.position();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SeekableReadableChannel<byte[]> shifted = shiftOffset(channel, pos);
        return asCharSequence(shifted, Integer.MAX_VALUE);
    }

    /** Ensure that the length is NOT greater than the amount of available data! */
    public static CharSequence asCharSequence(SeekableReadableChannel<byte[]> channel, int length) {
        return new CharSequenceOverSeekableReadableChannelOfBytes(channel, length);
    }

    /** Non-throwing version of {@link SeekableReadableChannel#position()} */
    public static long position(SeekableReadableChannel<?> channel) {
        long result;
        try {
            result = channel.position();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
