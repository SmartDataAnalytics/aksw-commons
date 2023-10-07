package org.aksw.commons.path.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public interface PathOps<T, P extends Path<T>> {

    P upcast(Path<T> path);

    List<T> getBasePathSegments();
    Comparator<T> getComparator();

    /** Create a new absolute or relative path based on the given segments */
    P newPath(boolean isAbsolute, List<T> segments);
    // T requireSubType(Path other);

    /**
     * Method to create a string from an argument of type T.
     * In most other cases, this method should return a relative path with the argument as the only segment.
     * However, if T is {@link String}(-like) then this method should parse the argument into an appropriate path.
     *
     * Implementations of {@link Path#resolve(Object)} should rely on this method.
     */
    default P create(T arg) {
        return newRelativePath(arg);
    }

    /** Create a path from an instance of T.
     * Note that T may not by specific to segments - e.g. a string can denote both a full path or a single segment */
    // P newPath(T element);

    /** To token for a path to refer to itself, such as '.' */
    T getSelfToken();

    /** The path segment to navigate to the parent, such as '..' */
    T getParentToken();

    /** Serialize a path as a string */
    String toString(P path);

    default String toStringRaw(Object path) {
        @SuppressWarnings("unchecked")
        P p = (P)path;
        return toString(p);
    }

    /** Deserialize a string into a path */
    P fromString(String str);

    /** Convenience shorthands for {@link #newPath(Object)} */
    default P newAbsolutePath(T segment) {
        return newAbsolutePath(Collections.singletonList(segment));
    }

    default P newAbsolutePath(@SuppressWarnings("unchecked") T ... segments) {
        return newAbsolutePath(Arrays.asList(segments));
    }

    default P newAbsolutePath(List<T> segments) {
        return newPath(true, segments);
    }

    default P newRelativePath(T segment) {
        return newRelativePath(Collections.singletonList(segment));
    }

    default P newRelativePath(@SuppressWarnings("unchecked") T ... segments) {
        return newRelativePath(Arrays.asList(segments));
    }

    default P newRelativePath(List<T> segments) {
        return newPath(false, segments);
    }
}
