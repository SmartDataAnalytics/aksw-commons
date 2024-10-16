package org.aksw.commons.path.nio;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/**
 * Base class for wrappers of file systems.
 * Implementations need to implement {@link #getDelegate()}.
 */
public abstract class FileSystemWrapperBase
    extends FileSystem
{
    protected abstract FileSystem getDelegate();

    public Path wrap(Path path) {
        return new PathWrapper<>(path, this);
    }

    public Path unwrap(Path path) {
        return unwrap(path, false);
    }

    public static Path unwrap(Path path, boolean repeatedly) {
        Path result = path;
        while (result instanceof PathWrapper pw) {
            result = pw.getDelegate();

            if (!repeatedly) {
                break;
            }
        }
        return result;
    }

    @Override
    public FileSystemProvider provider() {
        return getDelegate().provider();
    }

    @Override
    public void close() throws IOException {
        getDelegate().close();
    }

    @Override
    public boolean isOpen() {
        return getDelegate().isOpen();
    }

    @Override
    public boolean isReadOnly() {
        return getDelegate().isReadOnly();
    }

    @Override
    public String getSeparator() {
        return getDelegate().getSeparator();
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return () -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(getDelegate().getRootDirectories().iterator(), 0), false)
            .map(this::wrap)
            .iterator();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return getDelegate().getFileStores();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return getDelegate().supportedFileAttributeViews();
    }

    @Override
    public Path getPath(String first, String... more) {
        return wrap(getDelegate().getPath(first, more));
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        return getDelegate().getPathMatcher(syntaxAndPattern);
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return getDelegate().getUserPrincipalLookupService();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return getDelegate().newWatchService();
    }
}
