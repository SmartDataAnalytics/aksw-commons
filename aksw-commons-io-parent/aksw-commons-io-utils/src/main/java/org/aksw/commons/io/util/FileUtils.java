package org.aksw.commons.io.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;

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
     * Delete a specific path and then - regardless of deletion outcom e -try to delete all empty directories up to a given baseFolder.
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
                if (!Files.isDirectory(path)) {
                    throw new IllegalArgumentException("Path must be a directory: " + path);
                }

                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    // Ignore
                    break;
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

}
