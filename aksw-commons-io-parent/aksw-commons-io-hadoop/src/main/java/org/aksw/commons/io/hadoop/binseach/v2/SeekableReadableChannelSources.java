package org.aksw.commons.io.hadoop.binseach.v2;

import java.nio.file.Path;

import org.aksw.commons.io.input.SeekableReadableChannelSource;

public class SeekableReadableChannelSources {
    public static SeekableReadableChannelSource<byte[]> of(Path path) {
        return new SeekableReadableChannelSourceOverNio(path);
    }
}
