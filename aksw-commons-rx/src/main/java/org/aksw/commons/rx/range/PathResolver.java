package org.aksw.commons.rx.range;

import java.nio.file.Path;

public interface PathResolver {
    Path resolve(Path base, Iterable<String> segments);
}
