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

    @Override
    public PathStr create(String arg) {
        return fromString(arg);
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
    public String toStringRaw(Object path) {
        return toString((PathStr)path);
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

        // TODO Check for odd number of escape chars!
        String[] rawSegments = str.split("(?<!\\\\)/");
        List<String> segments = Arrays.asList(rawSegments).stream()
                .map(this::unescapeSegment)
                .filter(x -> !x.isEmpty()) // Filter out empty segments
                .collect(Collectors.toList());

        return newPath(isAbsolute, segments);
    }
}
