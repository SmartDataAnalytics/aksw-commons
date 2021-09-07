package org.aksw.commons.path.core;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class PathOpsStr
    implements PathOps<String, PathStr>, Serializable
{
    private static final long serialVersionUID = 1L;


    private static PathOpsStr INSTANCE = null;

    public static PathOpsStr get() {
        if (INSTANCE == null) {
            synchronized (PathOpsStr.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PathOpsStr();
                }
            }
        }
        return INSTANCE;
    }


    /** Convenience static shorthand for .get().newRoot() */
    public static PathStr newAbsolutePath() {
        return get().newRoot();
    }

    public static PathStr newRelativePath() {
        return get().newPath(false, Collections.emptyList());
    }

    public static PathStr create(String str) {
        return get().fromString(str);
    }



    @Override
    public PathStr upcast(Path<String> path) {
        return (PathStr)path;
    }

    @Override
    public List<String> getBasePathSegments() {
        return Collections.emptyList();
    }

    @Override
    public Comparator<String> getComparator() {
        return Comparator.naturalOrder();
    }

    @Override
    public PathStr newPath(boolean isAbsolute, List<String> segments) {
        return new PathStr(this, isAbsolute, segments);
    }

    @Override
    public PathStr newPath(String element) {
        return fromString(element);
    }

    @Override
    public String getSelfToken() {
        return ".";
    }

    @Override
    public String getParentToken() {
        return "..";
    }

    protected String escapeSegment(String str) {
        String result = str
                .replace("\\", "\\\\")
                .replace("/", "\\/");

        return result;
    }

    protected String unescapeSegment(String str) {
        String result = str
                .replace("\\/", "/")
                .replace("\\\\", "\\");

        return result;
    }



    @Override
    public String toString(PathStr path) {
        String result = (path.isAbsolute ? "/" : "")
                + path.getSegments().stream()
                    .map(this::escapeSegment)
                    .collect(Collectors.joining("/"));

        return result;
    }

    @Override
    public PathStr fromString(String str) {
        boolean isAbsolute = false;

        if (str.startsWith("/")) {
            isAbsolute = true;
            str = str.substring(1);
        }

        String[] rawSegments = str.split("(?<!\\\\)/");
        List<String> segments = Arrays.asList(rawSegments).stream()
                .map(this::unescapeSegment)
                .filter(x -> !x.isEmpty()) // Filter out empty segments
                .collect(Collectors.toList());

        return newPath(isAbsolute, segments);
    }

}
