package org.aksw.commons.io.util;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

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

}
