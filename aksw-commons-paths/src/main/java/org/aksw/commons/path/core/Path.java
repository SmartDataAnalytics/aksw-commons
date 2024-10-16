package org.aksw.commons.path.core;

import java.util.List;

/**
 * An interface summarized from {@link java.nio.file.Path}.
 * Purely provides the path manipulation functions - without FileSystem dependency.
 *
 * This enables the use of all the
 * path operations in different contexts, such as
 * for use as keys in hierarchical representations of RDF data (e.g. TreeGrids).
 *
 * @author Claus Stadler
 *
 * @param <T> The types of segments in the path
 */
public interface Path<T>
    extends Iterable<Path<T>>, Comparable<Path<T>>
{
    Path<T> toAbsolutePath();
    boolean isAbsolute();
    List<T> getSegments();
    // T getLastSegment();
    Path<T> getRoot();
    Path<T> getFileName();
    Path<T> getParent();
    int getNameCount();
    Path<T> getName(int index);

    Path<T> subpath(int beginIndex, int endIndex);

    /** Experimental. Not part of nio Path.*/
    Path<T> subpath(int beginIndex);

//    default Path<T> subpath(int beginIndex) {
//        return subpath(beginIndex, getNameCount());
//    }

    boolean startsWith(Path<T> other);
    boolean endsWith(Path<T> other);

    Path<T> normalize();
    Path<T> resolveStr(String other);
    Path<T> resolve(T other);
    Path<T> resolve(Path<T> other);

    Path<T> resolveSiblingStr(String other);
    Path<T> resolveSibling(T other);
    Path<T> resolveSibling(Path<T> other);

    Path<T> relativize(Path<T> other);

    default T toSegment() {
        List<T> segments = getSegments();
        if (segments.size() != 1) {
            throw new IllegalStateException("toSegment() only allowed for paths with exactly one segment");
        }

        T result = segments.iterator().next();
        return result;
    }

    /**
     * Returns an object such as the file system underlying this path. Returns null if not applicable.
     * Experimental.
     */
    Object getSystem();
}
