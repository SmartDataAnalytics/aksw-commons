package org.aksw.commons.io.hadoop.binseach.v2;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.input.SeekableReadableChannel;
import org.aksw.commons.io.input.SeekableReadableChannelOverNio;
import org.aksw.commons.io.input.SeekableReadableChannelSource;

public class SeekableReadableChannelSourceOverNio
    implements SeekableReadableChannelSource<byte[]>
{
    protected Path path;

    public SeekableReadableChannelSourceOverNio(Path path) {
        super();
        this.path = path;
    }

    @Override
    public long size() throws IOException {
        return Files.size(path);
    }

    @Override
    public ArrayOps<byte[]> getArrayOps() {
        return ArrayOps.BYTE;
    }

    @Override
    public SeekableReadableChannel<byte[]> newReadableChannel() throws IOException {
        FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
        return new SeekableReadableChannelOverNio<>(fileChannel);
    }
}
