package org.aksw.commons.io.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

public class FileUtils {

    public static void deleteRecursivelyIfExists(Path path, RecursiveDeleteOption ... options) throws IOException {
        if (Files.exists(path)) {
            MoreFiles.deleteRecursively(path, options);
        }
    }

    /** Return the first ancestor of a path that exists.
     *  May be the path itself or one if its transitive parents.
     *  Returns null on null input.
     *  Use {@link Path#relativize(Path)} to obtain the folders that would
     *  have to be created.
     */
    public static Path getFirstExistingAncestor(Path path) {
        Path result = path == null
                    ? null
                    : Files.exists(path)
                        ? path
                        : getFirstExistingAncestor(path.getParent());
        return result;
    }

    /**
     * Delete a specific path and then - regardless of deletion outcome -try to delete all empty directories up to a given baseFolder.
     * Empty folders are only deleted if their path starts with the baseFolder
     *
     * The result is the same as of {@link Files#deleteIfExists(Path)}.
     */
    public static boolean deleteFileIfExistsAndThenDeleteEmptyFolders(Path path, Path baseFolder, boolean alsoDeleteBaseFolder) throws IOException {
        boolean result = Files.deleteIfExists(path);
        path = path.getParent();
        if (path != null) {
            deleteEmptyFolders(path, baseFolder, alsoDeleteBaseFolder);
        }

        return result;
    }

    /** Delete parent folders of 'path' that are descendants of baseFolder (depending on the flag inclusive or exclusive) */
    public static void deleteEmptyFolders(Path path, Path baseFolder, boolean alsoDeleteBase) {
        while (path.startsWith(baseFolder) && (alsoDeleteBase || !path.equals(baseFolder))) {
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    // There can be race conditions when threads concurrently create files
                    // and attempt to clean empty dirs - it seems its better to silently ignore errors
                    // throw new IllegalArgumentException("Path must be a directory: " + path);

                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // Ignore
                        break;
                    }
                }
            }
            path = path.getParent();
        }
    }

    /** Best-effort moveAtomic */
    public static void moveAtomic(Path srcFile, Path tgtPath) throws IOException {
        try {
            Files.move(srcFile, tgtPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            try (
                FileChannel srcChannel = FileChannel.open(srcFile, StandardOpenOption.READ);
                FileChannel tgtChannel = FileChannel.open(tgtPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                long n = srcChannel.size();
                FileChannelUtils.transferFromFully(tgtChannel, srcChannel, 0, n, null);
                tgtChannel.force(true);
            }

            Files.delete(srcFile);
        }
    }


    /** Delete a path if it is an empty directory */
    public void deleteDirectoryIfEmpty(Path path) throws IOException {
        boolean isDirectory = Files.isDirectory(path);
        if (isDirectory) {
            boolean isEmpty = Files.list(path).count() == 0;
            if (isEmpty) {
                Files.delete(path);
            }
        }
    }

    /** TODO Add: Recursively delete all empty folders
     * @throws IOException */



    /**
     * Return a list of files matching a given glob pattern and a base path
     *
     * @param basePath
     * @param glob
     * @return
     * @throws IOException
     */
    public static List<Path> listPaths(Path basePath, String glob) throws IOException {
        List<Path> result = null;
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(basePath, glob)) {
            result = Lists.newArrayList(stream.iterator());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T readObject(Path target) throws IOException, ClassNotFoundException {
        Object obj;
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(target, StandardOpenOption.READ))) {
            obj = in.readObject();
        }

        return (T)obj;
    }


    /** Most basic (and limited) serialization approach using ObjectOutputStream */
    public static void writeObject(Path target, Object obj) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(target, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
            out.writeObject(obj);
            out.flush();
        }
    }


    /**
     * A java.nio variant of apache commons-io FileUtils.sizeOfDirectory.
     *
     * @param dirPath
     * @return
     * @throws IOException
     */
    public static long sizeOfDirectory(Path dirPath) throws IOException {
        return sizeOfDirectory(dirPath, null);
    }


    /**
     * A java.nio variant of apache commons-io FileUtils.sizeOfDirectory.
     *
     * @param dirPath
     * @param fileMatcher Predicate to test whether to take a certain file's size into account for the total sum
     *   null will match any file.
     * @return
     * @throws IOException
     */
    public static long sizeOfDirectory(Path dirPath, PathMatcher fileMatcher) throws IOException {
        final AtomicLong totalSize = new AtomicLong(0L);

        Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                boolean isAccepted = fileMatcher == null || fileMatcher.matches(file);
                if (isAccepted) {
                    long contrib = attrs.size();
                    // System.out.println("Path: " + file + " size: " + contrib);
                    totalSize.addAndGet(contrib);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });

        return totalSize.get();
    }

    public static Stream<Path> ancestors(Path start, boolean reflexive) {
        Stream<Path> r = ancestors(start);
        if (!reflexive) {
            r = r.skip(1);
        }
        return r;
    }

    public static Stream<Path> ancestors(Path start) {
        return Stream.iterate(start, node -> node.getParent() != null, Path::getParent);
    }

    /**
     * Look for a file among all ancestor folders
     *
     * @param start
     * @param fileName
     * @return
     */
    public static Path findInAncestors(Path start, String fileName) {
        return ancestors(start)
            .filter(folder -> Files.exists(folder.resolve(fileName)))
            .map(folder -> folder.resolve(fileName))
            .findFirst()
            .orElse(null);
    }

}
