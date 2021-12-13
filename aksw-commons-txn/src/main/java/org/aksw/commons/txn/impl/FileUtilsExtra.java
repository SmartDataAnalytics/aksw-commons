package org.aksw.commons.txn.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.aksw.commons.lambda.throwing.ThrowingConsumer;

/**
 * Eventually these file utils should be consolidated with those in aksw-commons-io.
 * However, my feel is that first there should be a little framework that allows for
 * modeling actions that have multiple prerequisites such as the existence of certain folders.
 * These utils here only allow for a single prerequisite.
 *
 */
public class FileUtilsExtra {


    public static Stream<Path> streamFilenames(Path folder) throws IOException {
        return Files.list(folder)
            .map(path -> path.resolveSibling(FileSyncImpl.getBaseName(path.getFileName().toString())));
    }

    public static void ensureParentFolderExists(Path childPath, ThrowingConsumer<Path> action) throws IOException {
        Path parentPath = childPath.getParent();

        if (parentPath == null) {
            try {
                action.accept(childPath);
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else {
            ensureFolderExists(parentPath, tmpParentPath -> action.accept(childPath));
        }
    }

    /**
     * Repeatedly retry to create the given file's parent folder and then perform an action based on that folder.
     * Uses 10 retries with 100ms delay before failing.
     * Useful if another process concurrently perform a 'deleteEmptyFolders' action an the same folder
     *
     *
     * @param path
     * @param action
     * @throws IOException
     */
    public static void ensureFolderExists(Path folderPath, ThrowingConsumer<Path> action) throws IOException {
        // FIXME Use our retry util instead and return a retry action which can be reconfigured
        for (int i = 0; i < 10; ++i) {
            Files.createDirectories(folderPath);
            try {
                action.accept(folderPath);
                break;
            } catch (Exception e) {
                if (!Files.exists(folderPath)) {
                    // logger.debug("Retrying folder creation: " + folderPath);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    continue;
                } else {
                    throw new IOException(e);
                }
            }
        }
    }


}
