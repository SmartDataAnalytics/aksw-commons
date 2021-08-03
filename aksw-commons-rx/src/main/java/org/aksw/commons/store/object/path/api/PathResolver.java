package org.aksw.commons.store.object.path.api;

import java.nio.file.Path;

public interface PathResolver {
    Path resolve(Path base, Iterable<String> segments);
}
