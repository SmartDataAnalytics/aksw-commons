package org.aksw.commons.path.core;

import java.nio.file.FileSystem;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Implementation of the {@link Path} interface for {@link java.nio.file.Path}.
 * Use {@link #wrap(java.nio.file.Path)} to create an instance of this class.
 *
 * @author Claus Stadler
 *
 */
public class PathNio implements Path<String> // PathSys<String, FileSystem>
{
    protected java.nio.file.Path delegate;

    protected PathNio(java.nio.file.Path delegate) {
        super();
        this.delegate = delegate;
    }

    public java.nio.file.Path getDelegate() {
        return delegate;
    }

    public static PathNio wrap(java.nio.file.Path nioPath) {
        return new PathNio(nioPath);
    }

    protected PathNio wrapInternal(java.nio.file.Path nioPath) {
        return new PathNio(nioPath);
    }

    @Override
    public Path<String> toAbsolutePath() {
        return wrapInternal(getDelegate().toAbsolutePath());
    }

    @Override
    public boolean isAbsolute() {
        return getDelegate().isAbsolute();
    }

    @Override
    public List<String> getSegments() {
        // This is a copy of the code in PathUtils.getPathSegments
        // in order to avoid the dependency to the utils package
        int n = getDelegate().getNameCount();

        String[] result = new String[n];

        // The iterator is expected to yield n items
        Iterator<java.nio.file.Path> it = getDelegate().iterator();
        for (int i = 0; i < n; ++i) {
            String segment = it.next().toString();
            result[i] = segment;
        }
        return Arrays.asList(result);
    }

    @Override
    public Path<String> getRoot() {
        return wrapInternal(getDelegate().getRoot());
    }

    @Override
    public Path<String> getFileName() {
        return wrapInternal(getDelegate().getFileName());
    }

    @Override
    public Path<String> getParent() {
        return wrapInternal(getDelegate().getParent());
    }

    @Override
    public int getNameCount() {
        return getDelegate().getNameCount();
    }

    @Override
    public Path<String> getName(int index) {
        return wrapInternal(getDelegate().getName(index));
    }

    @Override
    public Path<String> subpath(int beginIndex, int endIndex) {
        return wrapInternal(getDelegate().subpath(beginIndex, endIndex));
    }

    @Override
    public Path<String> subpath(int beginIndex) {
        int endIndex = getNameCount();
        return subpath(beginIndex, endIndex);
    }

    @Override
    public boolean startsWith(Path<String> other) {
        return getDelegate().startsWith(((PathNio)other).getDelegate());
    }

    @Override
    public boolean endsWith(Path<String> other) {
        return getDelegate().endsWith(((PathNio)other).getDelegate());
    }

    @Override
    public Path<String> normalize() {
        return wrapInternal(getDelegate().normalize());
    }

    @Override
    public Path<String> resolve(String other) {
        return resolveStr(other);
    }

    @Override
    public Path<String> resolveStr(String other) {
        return wrapInternal(getDelegate().resolve(other));
    }

    @Override
    public Path<String> resolve(Path<String> other) {
        return wrapInternal(getDelegate().resolve(((PathNio)other).getDelegate()));
    }

    @Override
    public Path<String> resolveSibling(String other) {
        return resolveSiblingStr(other);
    }

    @Override
    public Path<String> resolveSiblingStr(String other) {
        return wrapInternal(getDelegate().resolveSibling(other));
    }

    @Override
    public Path<String> resolveSibling(Path<String> other) {
        return wrapInternal(getDelegate().resolveSibling(((PathNio)other).getDelegate()));
    }

    @Override
    public Path<String> relativize(Path<String> other) {
        return wrapInternal(getDelegate().relativize(((PathNio)other).getDelegate()));
    }

    @Override
    public Iterator<Path<String>> iterator() {
        Iterable<java.nio.file.Path> iterable = () -> getDelegate().iterator();
        return StreamSupport.stream(iterable.spliterator(), false)
            .map(this::wrapInternal)
            .map(item -> (Path<String>)item)
            .iterator();
    }

    @Override
    public int compareTo(Path<String> that) {
        return Comparator.nullsFirst((x, y) -> x.toString().compareTo(y.toString())).compare(this, that);
    }

    @Override
    public FileSystem getSystem() {
        return getDelegate().getFileSystem();
    }
}
