package org.aksw.commons.io.hadoop.binseach.v2;

import java.io.IOException;
import java.nio.file.Path;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.input.SeekableReadableChannel;
import org.aksw.commons.io.input.SeekableReadableChannelSource;
import org.apache.hadoop.io.compress.SplittableCompressionCodec;

public class BlockSource
    implements SeekableReadableChannelSource<byte[]>
{
    protected SeekableReadableChannelSource<byte[]> delegate;
    protected SplittableCompressionCodec codec;

    public BlockSource(SeekableReadableChannelSource<byte[]> delegate, SplittableCompressionCodec codec) {
        super();
        this.delegate = delegate;
        this.codec = codec;
    }

    public SeekableReadableChannelSource<byte[]> getDelegate() {
        return delegate;
    }

    @Override
    public long size() throws IOException {
        return getDelegate().size();
    }

    public static BlockSource of(Path path, SplittableCompressionCodec codec) {
        return of(SeekableReadableChannelSources.of(path), codec);
    }

    public static BlockSource of(SeekableReadableChannelSource<byte[]> delegate, SplittableCompressionCodec codec) {
        return new BlockSource(delegate, codec);
    }

    @Override
    public BlockSourceChannel newReadableChannel(long start, long end) throws IOException {
        return newReadableChannel(start, end, false);
    }

    public BlockSourceChannel newReadableChannel(long start, long end, boolean blockMode) throws IOException {
        SeekableReadableChannel<byte[]> channel = getDelegate().newReadableChannel();
        BlockSourceChannel result = new BlockSourceChannel(channel, codec, blockMode);
        if (start != 0) {
            result.position(start);
        }

        // SeekableReadableChannel<byte[]> result = SeekableReadableChannels.wrap(baseChannel);
//        BlockSourceChannel result = new BlockSourceChannel(baseChannel);
//        if (end != Long.MAX_VALUE) {
//            long limit = end - start;
//            // FIXME handle end
//            // result = ReadableChannels.limit(result, limit);
//        }

        // SeekableByteChannel result = new SeekableReadableChannelWithBlockTracking<>(baseChannel);
        // SeekableByteChannel result = baseChannel;

        return result;
    }

    @Override
    public ArrayOps<byte[]> getArrayOps() {
        return ArrayOps.BYTE;
    }

    @Override
    public BlockSourceChannel newReadableChannel(long offset) throws IOException {
        return newReadableChannel(offset, Long.MAX_VALUE);
    }

    /** block mode: return -2 when reaching a block boundary. */
    public BlockSourceChannel newReadableChannel(long offset, boolean blockMode) throws IOException {
        return newReadableChannel(offset, Long.MAX_VALUE, blockMode);
    }

    @Override
    public BlockSourceChannel newReadableChannel() throws IOException {
        return newReadableChannel(0);
    }
}
