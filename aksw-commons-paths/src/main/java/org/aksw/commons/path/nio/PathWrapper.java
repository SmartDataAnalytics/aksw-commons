package org.aksw.commons.path.nio;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Objects;

/** Wrapper for paths where the filesystem is a wrapper. */
public class PathWrapper<P extends Path, FS extends FileSystemWrapperBase>
    implements Path
{
    protected Path delegate;
    protected FS fileSystem;

    public PathWrapper(Path delegate, FS fileSystem) {
        super();
        this.delegate = Objects.requireNonNull(delegate);
        this.fileSystem = Objects.requireNonNull(fileSystem);
    }

    public Path getDelegate() {
        return delegate;
    }

    @SuppressWarnings("unchecked")
    protected P wrap(Path delegate) {
        return (P)fileSystem.wrap(delegate);
    }

    protected Path unwrap(Path delegate) {
        return fileSystem.unwrap(delegate);
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public boolean isAbsolute() {
        return getDelegate().isAbsolute();
    }

    @Override
    public Path getRoot() {
        return wrap(getDelegate().getRoot());
    }

    @Override
    public Path getFileName() {
        return wrap(getDelegate().getFileName());
    }

    @Override
    public P getParent() {
        return wrap(getDelegate().getParent());
    }

    @Override
    public int getNameCount() {
        return getDelegate().getNameCount();
    }

    @Override
    public P getName(int index) {
        return wrap(getDelegate().getName(index));
    }

    @Override
    public P subpath(int beginIndex, int endIndex) {
        return wrap(getDelegate().subpath(beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(Path other) {
        return getDelegate().startsWith(unwrap(other));
    }

    @Override
    public boolean endsWith(Path other) {
        return getDelegate().endsWith(unwrap(other));
    }

    @Override
    public P normalize() {
        return wrap(getDelegate().normalize());
    }

    @Override
    public P resolve(Path other) {
        return wrap(getDelegate().resolve(unwrap(other)));
    }

    @Override
    public P relativize(Path other) {
        return wrap(getDelegate().relativize(unwrap(other)));
    }

    @Override
    public URI toUri() {
        return getDelegate().toUri();
    }

    @Override
    public P toAbsolutePath() {
        return wrap(getDelegate().toAbsolutePath());
    }

    @Override
    public P toRealPath(LinkOption... options) throws IOException {
        return wrap(getDelegate().toRealPath(options));
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        return getDelegate().register(watcher, events, modifiers);
    }

    @Override
    public int compareTo(Path other) {
        return getDelegate().compareTo(unwrap(other));
    }
}
