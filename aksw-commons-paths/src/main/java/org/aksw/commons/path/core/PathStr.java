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
}
