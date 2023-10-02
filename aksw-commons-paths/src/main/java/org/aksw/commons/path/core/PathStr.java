package org.aksw.commons.path.core;

import java.util.List;

/**
 * A path implementations for strings. Mimics Unix paths.
 * Escaping:
 * String is split on '/'
 * Backspace '\' escapes '/'  i.e. \/ -&gt; /
 *
 * Example:
 *
 * {@code
 *   PathOpsStr.create("/hell\//o/world").getSegments() yields [hell/, o, world].
 * }
 *
 * @author raven
 *
 */
public class PathStr
    extends PathBase<String, PathStr>
{
    private static final long serialVersionUID = 1L;

    public PathStr(PathOps<String, PathStr> pathOps, boolean isAbsolute, List<String> segments) {
        super(pathOps, isAbsolute, segments);
    }

    /* Static convenience shorthands */

    public static PathStr parse(String str) {
        return PathOpsStr.get().fromString(str);
    }

    public static PathStr newAbsolutePath(String segment) {
        return PathOpsStr.get().newAbsolutePath(segment);
    }

    public static PathStr newAbsolutePath(String ... segments) {
        return PathOpsStr.get().newAbsolutePath(segments);
    }

    public static PathStr newAbsolutePath(List<String> segments) {
        return PathOpsStr.get().newAbsolutePath(segments);
    }

    public static PathStr newRelativePath(String segment) {
        return PathOpsStr.get().newRelativePath(segment);
    }

    public static PathStr newRelativePath(String ... segments) {
        return PathOpsStr.get().newRelativePath(segments);
    }

    public static PathStr newRelativePath(List<String> segments) {
        return PathOpsStr.get().newRelativePath(segments);
    }
}
