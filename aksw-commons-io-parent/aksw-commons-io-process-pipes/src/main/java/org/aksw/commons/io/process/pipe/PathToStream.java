package org.aksw.commons.io.process.pipe;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Function;

public interface PathToStream {
    ProcessSink apply(Path src);

    default Function<Path, InputStream> asStreamSource() {
        return path -> apply(path).getInputStream();
    }
}