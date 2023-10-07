package org.aksw.commons.path.core;

import java.util.Comparator;
import java.util.List;

public interface PathOpsWrapper<T, P extends Path<T>>
    extends PathOps<T, P>
{
    PathOps<T, P> getDelegate();

    @Override
    default P upcast(Path<T> path) {
        return getDelegate().upcast(path);
    }

    @Override
    default List<T> getBasePathSegments() {
        return getDelegate().getBasePathSegments();
    }

    @Override
    default Comparator<T> getComparator() {
        return getDelegate().getComparator();
    }

    @Override
    default P newPath(boolean isAbsolute, List<T> segments) {
        return getDelegate().newPath(isAbsolute, segments);
    }

    @Override
    default T getSelfToken() {
        return getDelegate().getSelfToken();
    }

    @Override
    default T getParentToken() {
        return getDelegate().getParentToken();
    }

    @Override
    default String toString(P path) {
        return getDelegate().toString(path);
    }

    @Override
    default String toStringRaw(Object path) {
        return getDelegate().toStringRaw(path);
    }

    @Override
    default P fromString(String str) {
        return getDelegate().fromString(str);
    }
}
