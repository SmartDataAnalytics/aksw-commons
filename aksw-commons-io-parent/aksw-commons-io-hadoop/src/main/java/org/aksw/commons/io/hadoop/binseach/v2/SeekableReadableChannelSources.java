package org.aksw.commons.io.hadoop.binseach.v2;

import java.nio.file.Path;

import org.aksw.commons.io.input.SeekableReadableChannelSource;
import org.aksw.commons.io.input.SeekableReadableSourceWithMonitor;

public class SeekableReadableChannelSources {
    public static SeekableReadableChannelSource<byte[]> of(Path path) {
        return new SeekableReadableChannelSourceOverNio(path);
    }

    public static <A, X extends SeekableReadableChannelSource<A>> SeekableReadableSourceWithMonitor<A, X> monitor(X delegate) {
        return new SeekableReadableSourceWithMonitor<>(delegate);
    }
}
