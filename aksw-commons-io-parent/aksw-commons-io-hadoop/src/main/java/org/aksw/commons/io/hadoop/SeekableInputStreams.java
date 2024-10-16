package org.aksw.commons.io.hadoop;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.input.GetPosition;
import org.aksw.commons.io.input.InputStreamOverChannel;
import org.aksw.commons.io.input.SeekableReadableChannel;
import org.aksw.commons.io.input.SeekableReadableChannelBase;
import org.aksw.commons.io.input.SeekableReadableChannels;
import org.aksw.commons.io.input.SetPosition;
import org.apache.hadoop.fs.Seekable;

public class SeekableInputStreams
{
    /* Functional interfaces that declare throwing of IOExceptions */

    @FunctionalInterface
    public interface GetPositionFn<T> { long apply(T entity) throws IOException; }

    @FunctionalInterface
    public interface SetPositionFn<T> { void apply(T entity, long position) throws IOException; }

    public static Seekable createSeekable(GetPosition getPosition, SetPosition setPosition) {
        return new Seekable() {
            @Override public void seek(long pos) throws IOException { setPosition.accept(pos); }
            @Override public long getPos() throws IOException { return getPosition.call(); }

            @Override
            public boolean seekToNewSource(long targetPos) throws IOException {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static <T extends ReadableByteChannel> SeekableInputStream create(
            T channel,
            GetPositionFn<? super T> getPosition,
            SetPositionFn<? super T> setPosition
    ) {
        return create(
                // Channels.newInputStream(channel), relies on size() which is not always implemented
                new InputStreamOverChannel(channel),
                () -> getPosition.apply(channel),
                position -> setPosition.apply(channel, position));

    }

    /** Bridge SeekableInputStream to the channel API */
    public static SeekableReadableChannel<byte[]> wrap(SeekableInputStream in) {
        Objects.requireNonNull(in);
        return new SeekableReadableChannelBase<>() {
            @Override
            public SeekableReadableChannel<byte[]> cloneObject() {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read(byte[] array, int position, int length) throws IOException {
                return in.read(array, position, length);
            }

            @Override
            public ArrayOps<byte[]> getArrayOps() {
                return ArrayOps.BYTE;
            }

            @Override
            public long position() {
                return in.position();
            }

            @Override
            public void position(long pos) {
                in.position(pos);
            }
            @Override
            protected void closeActual() throws Exception {
                in.close();
            }
        };
    }

    public static <T extends ReadableByteChannel> SeekableInputStream create(SeekableByteChannel channel) {
        return create(channel, SeekableByteChannel::position, SeekableByteChannel::position);
    }

    public static <T extends ReadableByteChannel> SeekableInputStream create(SeekableReadableChannel<byte[]> channel) {
        SeekableByteChannel adaptedChannel = SeekableReadableChannels.adapt(channel);
        return create(adaptedChannel);
    }


    public static <T extends ReadableByteChannel> SeekableInputStream create(
        InputStream in,
        GetPosition getPosition,
        SetPosition setPosition
    ) {
        Seekable seekable = createSeekable(getPosition, setPosition);
        return new SeekableInputStream(in, seekable);
    }


    public static SeekableInputStream create(
        InputStream in,
        Seekable seekable
    ) {
        return new SeekableInputStream(in, seekable);
    }


    /**
     * The argument for invoking this methods must be a seekable input streams that implements
     * hadoop's protocol for splittable codecs using READ_MODE.BYBLOCK.
     *
     * Whereas hadoop's protocol will always read 1 byte beyond the split boundary,
     * this wrapper will stop exactly at that boundary. Internally a push-back input stream is used
     * to push that single "read-ahead" byte back once it is encountered.
     *
     * A block boundary is advertised by a call to read() by returing -2.
     * This return value indicates that read() may be
     * called again and will return at least one more byte.
     * A return value of -1 indicates "end of file" just as usual.
     *
     * @param decodedIn
     * @param endOfBlockMarker The value to return when an end of block is detected
     * @return
     * @throws IOException
     */
    public static ReadableChannelWithBlockAdvertisement advertiseEndOfBlock(InputStream decodedIn) throws IOException {
        return advertiseEndOfBlock(decodedIn, -2);
    }

    public static ReadableChannelWithBlockAdvertisement advertiseEndOfBlock(InputStream decodedIn, int endOfBlockMarker) throws IOException {
        return new ReadableChannelWithBlockAdvertisement(decodedIn, endOfBlockMarker);
    }

    /**
     * Only for use in combination with {@link #advertiseEndOfBlock(InputStream, int)}:
     * Report end of stream if a given position is exceeded.
     */
//    public ReadableByteChannel limitByPosition() {
//
//    }
}
