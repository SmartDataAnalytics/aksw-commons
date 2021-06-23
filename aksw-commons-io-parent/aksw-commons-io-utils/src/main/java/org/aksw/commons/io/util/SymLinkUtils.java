package org.aksw.commons.io.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.io.util.symlink.SymbolicLinkStrategy;

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
     * Return the new link or or all prior existing link(s)
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
    		String prefix,
    		String suffix) throws IOException {
        Path sourceFolder = rawSourceFolder.normalize();
        Path target = rawTarget.normalize();

        Path relTgt = sourceFolder.relativize(target);

        Path absTarget = target.toAbsolutePath();
//		Path folder = rawFolder.normalize();
//		Path file = rawFile.normalize().relativize(folder);

        //System.out.println("Realtivation: " + file.relativize(folder));

        Map<Path, Path> existingSymLinks = readSymbolicLinks(symlinkStrategy, rawSourceFolder, prefix, suffix);

        Collection<Path> result = existingSymLinks.entrySet().stream()
                .filter(e -> {
                    Path absCand = resolveSymLinkAbsolute(e.getKey(), e.getValue()); //e.getKey().getParent().resolve(e.getValue()).normalize().toAbsolutePath();
                    boolean r = absCand.equals(absTarget);
                    return r;
                })
                .map(Entry::getKey)
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
                	symlinkStrategy.createSymbolicLink(c, relTgt);
                    // Files.createSymbolicLink(c, relTgt);
                    result = Collections.singleton(c);
                    break;
                }
            }
        }

        return result;
    }
    
    
    public static Stream<Entry<Path, Path>> streamSymbolicLinks(
    		SymbolicLinkStrategy symlinkStrategy,
    		Path sourceFolder,
    		String prefix,
    		String suffix) throws IOException {

        Stream<Entry<Path, Path>> result = Files.list(sourceFolder)
            .filter(symlinkStrategy::isSymbolicLink)
            .filter(path -> {
                String fileName = path.getFileName().toString();

                boolean r = fileName.startsWith(prefix) && fileName.endsWith(suffix);
                // TODO Check that the string between prefix and suffix is either an empty string
                // or corresponds to a number
                return r;
            })
            .flatMap(path -> {
                Stream<Entry<Path, Path>> r;
                try {
                    r = Stream.of(new SimpleEntry<>(path, symlinkStrategy.readSymbolicLink(path)));
                } catch (IOException e) {
                    // logger.warn("Error reading symoblic link; skipping", e);
                    r = Stream.empty();
                }
                return r;
            });

        return result;
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
    public static Map<Path, Path> readSymbolicLinks(
    		SymbolicLinkStrategy symlinkStrategy,
    		Path sourceFolder,
    		String prefix,
    		String suffix) throws IOException {
        try (Stream<Entry<Path, Path>> stream = streamSymbolicLinks(symlinkStrategy, sourceFolder, prefix, suffix)) {
	    	Map<Path, Path> result = stream
	                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	        return result;
        }
    }

}
