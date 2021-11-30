package org.aksw.commons.io.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;

public class PathUtils {

    public static Path resolve(Path basePath, String... segments) {
        return PathUtils.resolve(basePath, Arrays.asList(segments));
    }

    public static Path resolve(Path basePath, Iterable<String> segments) {
        Path result = basePath;
        for (String segment : segments) {
            result = result.resolve(segment);
        }

        return result;
    }

    public static String[] getPathSegments(Path path) {
        int n = path.getNameCount();

        String[] result = new String[n];

        // The iterator is expected to yield n items
        Iterator<Path> it = path.iterator();
        for (int i = 0; i < n; ++i) {
            String segment = it.next().toString();
            result[i] = segment;
        }
        return result;
    }

    public static String[] splitBySlash(String str) {
        String[] result = Arrays.asList(str.split("/+")).stream()
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toList())
                .toArray(new String[0]);

        return result;
    }

    public static String join(String... segments) {
        String result = Arrays.asList(segments).stream().collect(Collectors.joining("/"));
        return result;
    }

    /**
     * Create a path on a (possibly remote) file system via Java nio.
     *
     * @param fsUri The url to a file system. Null or blank for the local one.
     * @param pathStr A path on the file system.
     * @return A pair comprising the path and a close action which closes the underlying file system.
     * @throws IOException
     */
    public static Entry<Path, Closeable> resolveFsAndPath(String fsUri, String pathStr) throws IOException {
        Path dbPath = null;

        FileSystem fs;
        Closeable fsCloseActionTmp;
        if (fsUri != null && !fsUri.isBlank()) {
            fs = FileSystems.newFileSystem(URI.create(fsUri), Collections.emptyMap());
            fsCloseActionTmp = () -> fs.close();
        } else {
            fs = FileSystems.getDefault();
            fsCloseActionTmp = () -> {}; // noop
        }

        Closeable closeAction = fsCloseActionTmp;

        try {
            if (pathStr != null && !pathStr.isBlank()) {
                dbPath = fs.getPath(pathStr).toAbsolutePath();
//                for (Path root : fs.getRootDirectories()) {
//                    dbPath = root.resolve(pathStr);
//                    // Only consider the first root (if any)
//                    break;
//                }
            }
        } catch (Exception e) {
            try {
                closeAction.close();
            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }

            throw new RuntimeException(e);
        }

        return Maps.immutableEntry(dbPath, closeAction);
    }

}
