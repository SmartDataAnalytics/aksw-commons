package org.aksw.commons.path.nio;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.aksw.commons.path.core.PathSys;

public class FileSystemSpec {
    protected Map<String, ?> env;
    protected ClassLoader loader;

    protected URI uri;
    protected PathSys<String, FileSystemSpec> basePath;

    protected FileSystemSpec(Map<String, ?> env, ClassLoader loader, PathSys<String, FileSystemSpec> basePath) {
        super();
        this.env = env;
        this.loader = loader;
        this.basePath = Objects.requireNonNull(basePath);
    }

    protected FileSystemSpec(Map<String, ?> env, ClassLoader loader, URI uri) {
        super();
        this.env = env;
        this.loader = loader;
        this.uri = Objects.requireNonNull(uri);
    }

    public static FileSystemSpec of(Map<String, ?> env, ClassLoader loader, URI uri) {
        return new FileSystemSpec(env, loader, uri);
    }

    public static FileSystemSpec of(Map<String, ?> env, ClassLoader loader, PathSys<String, FileSystemSpec> basePath) {
        return new FileSystemSpec(env, loader, basePath);
    }

    public boolean isUri() {
        return uri != null;
    }

    public boolean isPath() {
        return basePath != null;
    }

    public FileSystem resolve() throws IOException {
        FileSystem result;
        if (isUri()) {
            result = new FileSystemWithSpec(FileSystems.newFileSystem(uri, env, loader), this);
        } else {
            FileSystemSpec baseSpec = basePath.getSystem();
            FileSystem baseFs = baseSpec.resolve();
            List<String> segments = basePath.getSegments();
            String first = segments.isEmpty() ? "" : segments.get(0);
            String[] rest = segments.isEmpty() ? new String[0] : segments.subList(1, segments.size()).toArray(new String[0]);
            Path path = baseFs.getPath(first, rest);
            FileSystem outer = new FileSystemWithSpec(FileSystems.newFileSystem(path, env, loader), this);
            result = new FileSystem2(outer, baseFs);
        }
        return result;
    }
}
