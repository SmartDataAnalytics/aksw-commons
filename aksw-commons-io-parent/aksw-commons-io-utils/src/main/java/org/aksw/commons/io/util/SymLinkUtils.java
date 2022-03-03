package org.aksw.commons.io.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.io.util.symlink.SymbolicLinkStrategy;
import org.aksw.commons.lambda.throwing.ThrowingBiFunction;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.collect.Tables;

public class SymLinkUtils {
    /**
     * Read a symbolic link and return and absolute path to its target.
     *
     * @param symLink
     * @return
     * @throws IOException
     */
    public static Path readSymLinkAbsolute(Path symLink) throws IOException {
        if (!Files.isSymbolicLink(symLink)) {
            throw new IllegalArgumentException("Not a symbolic link: " + symLink);
        }
        Path symLinkTgt = Files.readSymbolicLink(symLink);

        Path result = resolveSymLinkAbsolute(symLink, symLinkTgt);
        return result;
    }

    /**
     * Given a path that is considered a symlink and its target, return the absolute path
     * obtained by resolving the target (which may be a relative path) against the symlink.
     *
     * This method allows resolving a relative symlinkTgt of a different file system (e.g. UNIX) against
     * a symLinkSrc (e.g. WebDAV)
     *
     * @param symLinkSrc
     * @param symLinkTgt
     * @return
     */
    public static Path resolveSymLinkAbsolute(Path symLinkSrc, Path symLinkTgt) {
        // resolveSibling instead of getParent.resolve?

        Path result;
        if (symLinkTgt.isAbsolute()) {
            result = symLinkTgt;
        } else {
            String[] tgtSegments = PathUtils.getPathSegments(symLinkTgt);
            result = PathUtils.resolve(symLinkSrc.getParent(), tgtSegments).normalize().toAbsolutePath();
        }
        return result;
    }

    /**
     * Within 'folder' create a link to 'file' with name 'baseName' if it does not yet exist.
     * Return the new link or all prior existing link(s)
     *
     * @param file
     * @param folder
     * @param baseName
     * @return
     * @throws IOException
     */
    public static Collection<Path> allocateSymbolicLink(
            SymbolicLinkStrategy symlinkStrategy,
            Path rawTarget,
            Path rawSourceFolder,
            Function<String, String> fileNameNormalizer,
            String prefix,
            String suffix
            ) throws Exception {

        return allocateSymbolicLink(symlinkStrategy, rawTarget, rawSourceFolder, fileNameNormalizer, prefix, suffix,
                (file, tgt) -> {
                    symlinkStrategy.createSymbolicLink(file, tgt);
                    return file;
                });
    }


    public static Collection<Path> allocateSymbolicLink(
            SymbolicLinkStrategy symlinkStrategy,
            Path rawTarget,
            Path rawSourceFolder,
            Function<String, String> fileNameNormalizer,
            String prefix,
            String suffix,
            ThrowingBiFunction<Path, Path, Path> tgtAndContentToFile // Called when a new symlink is allocated
            ) throws Exception {
        Path sourceFolder = rawSourceFolder.normalize();
        Path target = rawTarget.normalize();

        Path relTgt = sourceFolder.relativize(target);

        Path absTarget = target.toAbsolutePath();
//		Path folder = rawFolder.normalize();
//		Path file = rawFile.normalize().relativize(folder);

        //System.out.println("Realtivation: " + file.relativize(folder));

        Table<String, Path, Path> existingSymLinks = readSymbolicLinks(symlinkStrategy, rawSourceFolder, fileNameNormalizer, prefix, suffix);

        Collection<Path> result = existingSymLinks.cellSet().stream()
                .filter(e -> {
                    Path absCand = resolveSymLinkAbsolute(e.getColumnKey(), e.getValue()); //e.getKey().getParent().resolve(e.getValue()).normalize().toAbsolutePath();
                    boolean r = absCand.equals(absTarget);
                    return r;
                })
                .map(Cell::getColumnKey)
                .collect(Collectors.toSet());

        // Check all symlinks in the folder whether any points to target
//        Collection<Path> result = Files.list(sourceFolder)
//            .filter(Files::isSymbolicLink)
//            .filter(t -> {
//                Path tgt;
//                try {
//                     tgt = Files.readSymbolicLink(t);
//                     tgt = tgt.toAbsolutePath();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//
//                boolean r = Objects.equals(absTarget, tgt);
//                return r;
//            })
//            .collect(Collectors.toList());

        if (result.isEmpty()) {
            for(int i = 1; ; ++i) {
                String cand = prefix + (i == 1 ? "" : i) + suffix;
                Path c = sourceFolder.resolve(cand);

                //Path relTgt = c.relativize(target);

                if (!Files.exists(c, LinkOption.NOFOLLOW_LINKS)) {
                    Path tmp = tgtAndContentToFile.apply(c, relTgt);
                    // symlinkStrategy.createSymbolicLink(c, relTgt);
                    // Files.createSymbolicLink(c, relTgt);
                    result = Collections.singleton(tmp);
                    break;
                }
            }
        }

        return result;
    }

    public static Function<Stream<Path>, Stream<Cell<String, Path, Path>>> streamSymbolicLinks(
            SymbolicLinkStrategy symlinkStrategy,
            Function<String, String> fileNameHarmonizer,
            String prefix,
            String suffix) throws IOException {

        return pathStream -> pathStream
            .filter(symlinkStrategy::isSymbolicLink)
            .flatMap(path -> {
                String rawFileName = path.getFileName().toString();
                String fileName = fileNameHarmonizer.apply(rawFileName);

                boolean isAccepted = fileName.startsWith(prefix) && fileName.endsWith(suffix);


                Stream<Cell<String, Path, Path>> r;
                if (isAccepted) {
                    try {
                        Path linkTarget = symlinkStrategy.readSymbolicLink(path);
                        r = Stream.of(Tables.immutableCell(fileName, path, linkTarget));
                    } catch (Exception e) {
                        // logger.warn("Failed to read a (virtual) symlink", e);
                        r = Stream.empty();
                    }
                } else {
                    r = Stream.empty();
                }

                return r;
            });
    }


    /**
     * Within 'sourceFolder' read all symbolic links with the pattern 'baseName${number}' and return a map
     * with their targets.
     *
     * @param rawSourceFolder
     * @param baseName
     * @return
     * @throws IOException
     */
    public static Table<String, Path, Path> readSymbolicLinks(
            SymbolicLinkStrategy symlinkStrategy,
            Path sourceFolder,
            Function<String, String> filenameNormalizer,
            String prefix,
            String suffix) throws IOException {
        Table<String, Path, Path> result = HashBasedTable.create();

        try (Stream<Path> stream = Files.list(sourceFolder)) {
            streamSymbolicLinks(symlinkStrategy, filenameNormalizer, prefix, suffix)
                .apply(stream)
                .forEach(cell -> result.put(cell.getRowKey(), cell.getColumnKey(), cell.getValue()));

        }

        return result;
    }

}
