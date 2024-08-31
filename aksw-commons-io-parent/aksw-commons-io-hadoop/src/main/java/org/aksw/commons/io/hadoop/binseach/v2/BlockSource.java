package org.aksw.commons.io.hadoop.binseach.v2;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.buffer.plain.Buffer;
import org.aksw.commons.io.input.SeekableReadableChannelSource;
import org.apache.hadoop.io.compress.SplittableCompressionCodec;

import com.github.benmanes.caffeine.cache.Caffeine;

public class BlockSource
    implements SeekableReadableChannelSource<byte[]>
{
    protected Path path;
    protected SplittableCompressionCodec codec;

    protected Caffeine<Long, Buffer<byte[]>> x;


    public BlockSource(Path path, SplittableCompressionCodec codec) {
        super();
        this.path = path;
        this.codec = codec;
    }

    @Override
    public long size() throws IOException {
        return Files.size(path);
    }

    public static BlockSource of(Path path, SplittableCompressionCodec codec) {
        return new BlockSource(path, codec);
    }

    @Override
    public BlockSourceChannelAdapter newReadableChannel(long start, long end) throws IOException {
        return newReadableChannel(start, end, false);
    }

    public BlockSourceChannelAdapter newReadableChannel(long start, long end, boolean blockMode) throws IOException {
        FileChannel fileChannel;
        try {
            fileChannel = FileChannel.open(path, StandardOpenOption.READ);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BlockSourceChannel baseChannel = new BlockSourceChannel(fileChannel, codec, blockMode);
        if (start != 0) {
            baseChannel.position(start);
        }

        // SeekableReadableChannel<byte[]> result = SeekableReadableChannels.wrap(baseChannel);
        BlockSourceChannelAdapter result = new BlockSourceChannelAdapter(baseChannel);
        if (end != Long.MAX_VALUE) {
            long limit = end - start;
            // FIXME handle end
            // result = ReadableChannels.limit(result, limit);
        }

        // SeekableByteChannel result = new SeekableReadableChannelWithBlockTracking<>(baseChannel);
        // SeekableByteChannel result = baseChannel;

        return result;
    }

    @Override
    public ArrayOps<byte[]> getArrayOps() {
        return ArrayOps.BYTE;
    }

    @Override
    public BlockSourceChannelAdapter newReadableChannel(long offset) throws IOException {
        return newReadableChannel(offset, Long.MAX_VALUE);
    }

    /** block mode: return -2 when reaching a block boundary. */
    public BlockSourceChannelAdapter newReadableChannel(long offset, boolean blockMode) throws IOException {
        return newReadableChannel(offset, Long.MAX_VALUE, blockMode);
    }

    @Override
    public BlockSourceChannelAdapter newReadableChannel() throws IOException {
        return newReadableChannel(0);
    }
}
