package org.aksw.commons.store.object.key.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.aksw.commons.io.util.PathUtils;
import org.aksw.commons.store.object.key.api.KeyObjectStore;
import org.aksw.commons.store.object.path.api.ObjectFileStore;
import org.aksw.commons.store.object.path.api.PathResolver;
import org.aksw.commons.store.object.path.impl.ObjectFileStoreNative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyObjectStoreImpl
    implements KeyObjectStore
{
    private static final Logger logger = LoggerFactory.getLogger(KeyObjectStoreImpl.class);

    protected Path rootPath;
    protected PathResolver pathResolver;
    protected ObjectFileStore objectFileStore;

    public KeyObjectStoreImpl(Path rootPath, PathResolver pathResolver, ObjectFileStore objectFileStore) {
        super();
        this.objectFileStore = objectFileStore;
        this.pathResolver = pathResolver;
        this.rootPath = rootPath;
    }

    @Override
    public void put(Iterable<String> keySegments, Object obj) throws IOException{
        Path absPath = pathResolver.resolve(rootPath, keySegments);
        Path parentPath = absPath.getParent();
        if (parentPath != null) {
            Files.createDirectories(parentPath);
        }
        objectFileStore.write(absPath, obj);
    }

    @Override
    public <T> T get(Iterable<String> keySegments) throws IOException, ClassNotFoundException{
        Path absPath = pathResolver.resolve(rootPath, keySegments);
        T result = objectFileStore.readAs(absPath);
        logger.debug("Loading object from " + absPath);
        return result;
    }


    protected <T> T load(Path absPath) throws ClassNotFoundException, IOException {
        T result = objectFileStore.readAs(absPath);
        logger.debug("Loading object from " + absPath);
        return result;

    }

    @Override
    public <T> T computeIfAbsent(Iterable<String> keySegments, Callable<T> initializer)
            throws ClassNotFoundException, IOException, ExecutionException {

        Path absPath = pathResolver.resolve(rootPath, keySegments);
        T result;
        try {
            result = Files.exists(absPath)
                   ? load(absPath)
                   : initializer.call();
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
        return result;
    }

    public static KeyObjectStore createSimple(Path rootPath) {
        return new KeyObjectStoreImpl(rootPath, PathUtils::resolve, new ObjectFileStoreNative());
    }

    public static KeyObjectStore create(Path rootPath, ObjectFileStore objectFileStore) {
        return new KeyObjectStoreImpl(rootPath, PathUtils::resolve, objectFileStore);
    }

}
