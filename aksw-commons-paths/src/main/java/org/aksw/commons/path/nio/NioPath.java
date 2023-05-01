package org.aksw.commons.path.nio;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.util.Objects;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

/**
 * An implementation of nio's {@link Path} over an {@link org.aksw.commons.path.core.Path}.
 * Intended for use as a base class for custom path implementations.
 *
 * @param <FS> The {@link FileSystem} type.
 */
public class NioPath<FS extends FileSystem>
    implements Path
{
    protected FS fileSystem;
    protected org.aksw.commons.path.core.Path<String> path;

    public NioPath(FS fileSystem, org.aksw.commons.path.core.Path<String> path) {
        super();
        this.path = path;
        this.fileSystem = fileSystem;
    }

    public org.aksw.commons.path.core.Path<String> getBackingPath() {
        return path;
    }

    @Override
    public FS getFileSystem() {
        return fileSystem;
    }

    protected NioPath<FS> newPath(org.aksw.commons.path.core.Path<String> path) {
        return new NioPath<>(fileSystem, path.getRoot());
    }

    protected org.aksw.commons.path.core.Path<String> extractPath(Path other) {
        org.aksw.commons.path.core.Path<String> result = other instanceof NioPath
                ? ((NioPath<?>)other).path
                : null;
        return result;
    }

    @Override
    public boolean isAbsolute() {
        return path.isAbsolute();
    }

    @Override
    public Path getRoot() {
        return newPath(path.getRoot());
    }

    @Override
    public Path getFileName() {
        return newPath(path.getFileName());
    }

    @Override
    public Path getParent() {
        return newPath(path.getParent());
    }

    @Override
    public int getNameCount() {
        return path.getNameCount();
    }

    @Override
    public Path getName(int index) {
        return newPath(path.getName(index));
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return newPath(path.subpath(beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(Path other) {
        org.aksw.commons.path.core.Path<String> otherPath = extractPath(other);
        return path.startsWith(otherPath);
    }

    @Override
    public boolean endsWith(Path other) {
        org.aksw.commons.path.core.Path<String> otherPath = extractPath(other);
        return path.endsWith(otherPath);
    }

    @Override
    public Path normalize() {
        return newPath(path.normalize());
    }

    @Override
    public Path resolve(Path other) {
        org.aksw.commons.path.core.Path<String> otherPath = extractPath(other);
        return newPath(path.resolve(otherPath));
    }

    @Override
    public Path relativize(Path other) {
        org.aksw.commons.path.core.Path<String> otherPath = extractPath(other);
        return newPath(path.relativize(otherPath));
    }

    @Override
    public URI toUri() {
        throw new UnsupportedOperationException("TODO"); // What's a proper schema?
    }

    @Override
    public Path toAbsolutePath() {
        return newPath(path.toAbsolutePath());

    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return this;
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(Path other) {
        org.aksw.commons.path.core.Path<String> otherPath = extractPath(other);
        return path.compareTo(otherPath);
    }

    @Override
    public String toString() {
        return Objects.toString(path);
        // return path + "@" + fileSystem;
    }
}
