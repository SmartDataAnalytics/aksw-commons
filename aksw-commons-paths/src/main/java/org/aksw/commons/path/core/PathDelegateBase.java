package org.aksw.commons.path.core;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;

/**
 * Base implementation for custom path wrappers with the main focus on custom values for the 'system' property.
 * This class implements all methods of {@link Path} and forwards all calls to the delegate.
 * The result of the operation on the delegate is then passed to the abstract {@link #wrap(Path)} function which can
 * then produce an appropriate implementation of the wrapper.
 */
public abstract class PathDelegateBase<T, F extends Path<T>>
    implements Path<T>
{
    // private static final long serialVersionUID = 1L;

    protected Path<T> delegate;

    protected F wrapOrNull(Path<T> basePath) {
        F result = basePath == null ? null : wrap(basePath);
        return result;
    }

    protected abstract F wrap(Path<T> basePath);

    protected Path<T> unwrap(Path<T> wrappedPath) {
        Path<T> result;
        Preconditions.checkArgument(wrappedPath instanceof PathDelegateBase, "Argument must be derived from PathDelegateBase");
        @SuppressWarnings("unchecked")
        PathDelegateBase<T, F> actualPath = (PathDelegateBase<T, F>)wrappedPath;
        result = actualPath.getDelegate();
        Object pathSystem = actualPath.getSystem();
        Preconditions.checkArgument(pathSystem == this.getSystem(), "Argument must have the same system (by referential equality using ==) as this path");
        return result;
    }

    public PathDelegateBase(Path<T> delegate) {
        this.delegate = delegate;
    }

    public Path<T> getDelegate() {
        return delegate;
    }

    @Override
    public Iterator<Path<T>> iterator() {
        return Streams.stream(getDelegate().iterator()).map(this::wrap).map(p -> (Path<T>)p).iterator();
        // return IteratorUtils.m getDelegate().iterator();
    }

    @Override
    public int compareTo(Path<T> o) {
        return getDelegate().compareTo(o);
    }

    @Override
    public F toAbsolutePath() {
        return wrapOrNull(getDelegate().toAbsolutePath());
    }

    @Override
    public boolean isAbsolute() {
        return getDelegate().isAbsolute();
    }

    @Override
    public List<T> getSegments() {
        return getDelegate().getSegments();
    }

    @Override
    public F getRoot() {
        return wrapOrNull(getDelegate().getRoot());
    }

    @Override
    public F getFileName() {
        return wrapOrNull(getDelegate().getFileName());
    }

    @Override
    public F getParent() {
        return wrapOrNull(getDelegate().getParent());
    }

    @Override
    public int getNameCount() {
        return getDelegate().getNameCount();
    }

    @Override
    public F getName(int index) {
        return wrapOrNull(getDelegate().getName(index));
    }

    @Override
    public F subpath(int beginIndex, int endIndex) {
        return wrapOrNull(getDelegate().subpath(beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(Path<T> other) {
        return getDelegate().startsWith(unwrap(other));
    }

    @Override
    public boolean endsWith(Path<T> other) {
        return getDelegate().endsWith(unwrap(other));
    }

    @Override
    public F normalize() {
        return wrapOrNull(getDelegate().normalize());
    }

    @Override
    public F resolveStr(String other) {
        return wrapOrNull(getDelegate().resolveStr(other));
    }

    @Override
    public F resolve(T other) {
        return wrapOrNull(getDelegate().resolve(other));
    }

    @Override
    public F resolve(Path<T> other) {
        return wrapOrNull(getDelegate().resolve(unwrap(other)));
    }

    @Override
    public F resolveSiblingStr(String other) {
        return wrapOrNull(getDelegate().resolveSiblingStr(other));
    }

    @Override
    public F resolveSibling(T other) {
        return wrapOrNull(getDelegate().resolveSibling(other));
    }

    @Override
    public F resolveSibling(Path<T> other) {
        return wrapOrNull(getDelegate().resolveSibling(unwrap(other)));
    }

    @Override
    public F relativize(Path<T> other) {
        return wrapOrNull(getDelegate().relativize(unwrap(other)));
    }

    @Override
    public Object getSystem() {
        return getDelegate().getSystem();
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PathDelegateBase<?, ?> other = (PathDelegateBase<?, ?>) obj;
        return Objects.equals(delegate, other.delegate);
    }
}
