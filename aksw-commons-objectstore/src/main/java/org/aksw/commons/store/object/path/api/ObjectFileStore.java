package org.aksw.commons.store.object.path.api;

import java.io.IOException;
import java.nio.file.Path;

public interface ObjectFileStore {
    void write(Path target, Object obj) throws IOException;
    Object read(Path source) throws IOException, ClassNotFoundException;

    @SuppressWarnings("unchecked")
    default <T> T readAs(Path source) throws IOException, ClassNotFoundException {
        Object tmp = read(source);
        return (T)tmp;
    }
}
