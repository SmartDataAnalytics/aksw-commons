package org.aksw.commons.util.lifecycle;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aksw.commons.util.exception.FinallyRunAll;
import org.aksw.commons.util.exception.FinallyRunAll.ThrowingConsumer;
import org.aksw.commons.util.function.ThrowingRunnable;

/**
 * A class where resources can be added.
 * Upon closing the resource manager, all registered resources will be freed.
 *
 * @implNote
 *   This implementation closes resources sequentially.
 */
public class ResourceMgr
    implements AutoCloseable
{
    private final Map<Object, ThrowingRunnable> resourceToCloser =
            Collections.synchronizedMap(new IdentityHashMap<>());

    private AtomicBoolean isClosed = new AtomicBoolean(false);

    public ResourceMgr() {
        super();
    }

    public <T extends AutoCloseable> T register(T closable) {
        return register(closable, AutoCloseable::close);
    }

    public <T> T register(T obj, ThrowingConsumer<? super T> closer) {
        return register(obj, () -> closer.accept(obj));
    }

    /**
     * If the resource manager has already been closed then resources are immediately closed
     * upon registration.
     */
    public <T> T register(T obj, ThrowingRunnable closeAction) {
        if (isClosed.get()) {
            try {
                closeAction.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            resourceToCloser.put(obj, closeAction);
        }
        return obj;
    }

    @Override
    public void close() {
        if (!isClosed.get()) {
            FinallyRunAll.runAll(resourceToCloser.entrySet(), e -> e.getValue().run(), null);
        }
    }

    public boolean isClosed() {
        return isClosed.get();
    }

    /** Create a path and register the filesystem with the resource manager. */
    public static Path toPath(ResourceMgr resourceMgr, Class<?> clz, String classPath) throws IOException {
        // GtfsMadridBench.class.getResource(name).toURI());
        URL url = clz.getResource(classPath);
        return toPath(resourceMgr, url);
    }

    /** Create a path and register the filesystem with the resource manager. */
    public static Path toPath(ResourceMgr resourceMgr, URL url) throws IOException {
        Objects.requireNonNull(url);
        try {
            return toPath(resourceMgr, url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // https://stackoverflow.com/a/36021165/160790
    /** Create a path and register the filesystem with the resource manager. */
    public static Path toPath(ResourceMgr resourceMgr, URI uri) throws IOException {
        Path result;
        try {
            Path rawPath = Paths.get(uri);
            result = fixPath(rawPath);
        }
        catch(FileSystemNotFoundException ex) {
            // TODO FileSystem needs to be closed
            FileSystem fs = FileSystems.newFileSystem(uri, Collections.<String,Object>emptyMap());
            resourceMgr.register(fs);
            result = fs.provider().getPath(uri);
        }
        return result;
    }

    public static Path fixPath(Path path) {
        Path result = path;
        Path parentPath = path.getParent();
        if(parentPath != null && !Files.exists(parentPath)) {
            Path fixedCandidatePath = path.resolve("/modules").resolve(path.getRoot().relativize(path));
            result = Files.exists(fixedCandidatePath) ? fixedCandidatePath : path;
        }
        return result;
    }
}
