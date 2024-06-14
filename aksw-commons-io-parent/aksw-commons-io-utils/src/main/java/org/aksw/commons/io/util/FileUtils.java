package org.aksw.commons.io.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.commons.lambda.throwing.ThrowingConsumer;

import com.google.common.collect.Lists;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

public class FileUtils {

    /** Attempt to open an output stream to the given file */
    @SuppressWarnings("resource")
    public static OutputStream newOutputStream(OutputConfig config) throws IOException {
        OutputStream result;
        String fileName = config.getTargetFile();
        boolean allowOverwrite = config.isOverwriteAllowed();

        if (fileName == null || "-".equals(fileName)) {
            result = StdIo.openStdOutWithCloseShield();
        } else {
            Path path = Path.of(fileName);
            if (allowOverwrite) {
                result = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                result = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW);
            }
        }

        return result;
    }

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

    /** Best-effort moveAtomic. Use moveAtomicIfPossible. */
    @Deprecated
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

    public static void moveAtomicIfSupported(Consumer<String> warnCallback, Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            if (warnCallback != null) {
                warnCallback.accept(String.format("Atomic move from %s to %s failed, falling back to copy", source, target));
            }
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Policies for when the target file already exists */
    public static enum OverwritePolicy {
        /** Raise an error */
        ERROR,

        /** Overwrite the target (unless the target is newer, then ignore) */
        OVERWRITE,

        /** Skip the write */
        SKIP,

        /** Overwrite the target (even if the target is newer) */
        OVERWRITE_ALWAYS,

        /** Overwrite the target, unless it was changed then raise an error */
        OVERWRITE_ERROR
    }

    // error on change
    // OVERWRITE_IF_NEWER (default) overwrite only if tmp file is newer than the existing file
    // 		(don't overwrite on concurrent change)
    // OVERWRITE_FORCE (overwrite even the existing file is newer than the tmp file)

    // keep tmp file on error

    public static void safeCreate(Path target, OverwritePolicy overwritePolicy, ThrowingConsumer<OutputStream> writer) throws Exception {
        safeCreate(target, null, overwritePolicy, writer);
    }

    public static FileTime getLastModifiedTimeOrNull(Path path) {
        // Get the time so that we can check whether the target was changed concurrently
        FileTime result = null;
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            result = attr.lastModifiedTime();
        } catch (Exception e) {
            // Ignore if failed
        }
        return result;
    }

    /**
     *
     * @param target
     * @param encoder A function that can wrap the OutputStream to the target file. May be null.
     * @param overwritePolicy
     * @param writer The action that writes to the (possibly encoded) output stream.
     * @throws Exception
     */
    public static void safeCreate(Path target, Function<OutputStream, OutputStream> encoder, OverwritePolicy overwritePolicy, ThrowingConsumer<OutputStream> writer) throws Exception {
        Objects.requireNonNull(overwritePolicy);

        boolean isCheckedOverwrite = OverwritePolicy.OVERWRITE.equals(overwritePolicy)
                || OverwritePolicy.OVERWRITE_ERROR.equals(overwritePolicy);

        boolean isOverwriteMode =  isCheckedOverwrite
                || OverwritePolicy.OVERWRITE_ALWAYS.equals(overwritePolicy);

        // Get the time so that we can check whether the target was changed concurrently
        FileTime targetFileTimeAtStart = isCheckedOverwrite ? getLastModifiedTimeOrNull(target) : null;

        String fileName = target.getFileName().toString();
        String randomPart = "." + new Random().nextInt();
        String tmpFileName = "." + fileName + randomPart + ".tmp";
        Path tmpFile = target.resolveSibling(tmpFileName);

        // true, false, null=not relevant
        Boolean fileExists = OverwritePolicy.SKIP.equals(overwritePolicy) || OverwritePolicy.ERROR.equals(overwritePolicy)
                ? Files.exists(target)
                : null;

        // Check whether the target already exists before we start writing the tmpFile
        if (Boolean.TRUE.equals(fileExists) && OverwritePolicy.ERROR.equals(overwritePolicy)) {
            throw new FileAlreadyExistsException(target.toAbsolutePath().toString());
        }

        if (!(Boolean.TRUE.equals(fileExists) && OverwritePolicy.SKIP.equals(overwritePolicy))) {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            boolean allowOverwrite = isOverwriteMode;
//            Thread hook = new Thread(() -> {
//                try {
//                    Files.deleteIfExists(tmpFile);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//            Runtime.getRuntime().addShutdownHook(hook);

            // Note: using a random id makes it highly unlikely that the tmp file already exists
            try (OutputStream raw = Files.newOutputStream(tmpFile, allowOverwrite ? StandardOpenOption.CREATE : StandardOpenOption.CREATE_NEW);
                 OutputStream out = encoder != null ? encoder.apply(raw) : raw) {
                writer.accept(out);
                out.flush();
            }

            boolean doFinalMove = true; // OverwriteMode.OVERWRITE_ALWAYS
            if (targetFileTimeAtStart != null) {
                if (OverwritePolicy.OVERWRITE_ERROR.equals(overwritePolicy)) {
                    // Raise an error if the target file was modified while creating the tmp file
                    FileTime targetFileTimeAtEnd = getLastModifiedTimeOrNull(target);
                    if (targetFileTimeAtEnd.compareTo(targetFileTimeAtStart) != 0) {
                        Files.delete(tmpFile);
                        throw new ConcurrentModificationException("Concurrent modification to file: " + target);
                    }
                } else if (OverwritePolicy.OVERWRITE.equals(overwritePolicy)) {
                    // Suppress overwrite if tmpFile is older than the target
                    FileTime tmpFileTime = getLastModifiedTimeOrNull(tmpFile);
                    if (tmpFileTime != null) {
                        if (tmpFileTime.compareTo(targetFileTimeAtStart) < 0) {
                            doFinalMove = false;
                        }
                    }
                }
            }

            if (doFinalMove) {
                moveAtomicIfSupported(null, tmpFile, target);
            } else {
                Files.delete(tmpFile);
            }
            // Runtime.getRuntime().removeShutdownHook(hook);
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
