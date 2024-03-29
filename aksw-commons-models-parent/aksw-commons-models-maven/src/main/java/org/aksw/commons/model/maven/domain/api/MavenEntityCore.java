package org.aksw.commons.model.maven.domain.api;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.aksw.commons.model.maven.domain.impl.MavenEntityCoreImpl;

/**
 * Interface for maven coordinates with an additional 'remainder' field.
 *
 * The remainder must be separated from the corrdinates using any of the following symbols:
 * <ul>
 *   <li>#</li>
 *   <li>/</li>
 *   <li>!</li>
 * </ul>
 *
 * This allows for making references to files within a maven artifact, such as:
 * {@code groupId:artifactId:version:jar:classifier!/path/to/file.txt}
 *
 * It also allows for retaining the remainder when transforming maven urns
 * into relative IRIs and resolving them against a base IRI:
 *
 * Resolution of {@code urn:mvn:groupId:artifactId:version:jar#dataset} against
 * {@code https://example.org} gives
 * {@code https://example.org/groupId/artifactId/artifactId-version.jar#dataset}.
 *
 */
public interface MavenEntityCore {

    public static final String URN_MVN = "urn:mvn:";

    String getGroupId();
    MavenEntityCore setGroupId(String groupId);

    String getArtifactId();
    MavenEntityCore setArtifactId(String artifactId);

    String getVersion();
    MavenEntityCore setVersion(String version);

    String getType();
    MavenEntityCore setType(String type);

    String getClassifier();
    MavenEntityCore setClassifier(String classifier);

    /** The remainder is a string that follows the maven coordinate and should start with # or / */
    String getRemainder();
    MavenEntityCore setRemainder(String remainder);


    /** Return a copy where all nulls replaced by empty strings and all strings are trimmed */
    public static MavenEntityCore normalize(MavenEntityCore coord) {
        String g = Objects.toString(coord.getGroupId(), "").trim();
        String a = Objects.toString(coord.getArtifactId(), "").trim();
        String v = Objects.toString(coord.getVersion(), "").trim();
        String c = Objects.toString(coord.getClassifier(), "").trim();
        String t = Objects.toString(coord.getType(), "").trim();
        String r = Objects.toString(coord.getRemainder(), "").trim();

        // If there is a classifier without a type then type becomes "jar"
        if (!c.isEmpty() && t.isEmpty()) {
            t = "jar";
        }

        return new MavenEntityCoreImpl(g, a, v, t, c, r);
    }

    public static String toString(MavenEntityCore coord) {
        String t = coord.getType();
        String c = coord.getClassifier();
        String r = coord.getRemainder();

        String suffix =
                (t == null || t.isEmpty() ? "" : ":" + t) +
                (c == null || c.isEmpty() ? "" : ":" + c) +
                (r == null || r.isEmpty() ? "" : r);

        String result = coord.getGroupId() + ":" + coord.getArtifactId() + ":" + coord.getVersion() + suffix;
        return result;
    }

    /** Returns a present classifier and type prefixed with '-' and '.', respectively. I.e.: [-classifier][.type] */
    public static String getFileNameSuffix(MavenEntityCore coord) {
        String t = coord.getType();
        String c = coord.getClassifier();

        String result =
                (c == null || c.isEmpty() ? "" : "-" + c) +
                (t == null || t.isEmpty() ? "" : "." + t);

        return result;
    }

    /**
     * Return a complete relative URL for the given coordinate, such as:
     * org/the/groupId/artifactId/version/artifactId-version-classifier.type
     */
    public static String toRelativeUrl(MavenEntityCore coord) {
        String result = toPath(coord) + "/" + toFileName(coord);
        return result;
    }

    /** Return the file name: artifactId-version-classifier.type */
    public static String toFileName(MavenEntityCore coord) {
        String suffix = getFileNameSuffix(coord);
        String v = coord.getVersion();
        String result = coord.getArtifactId() + (v == null || v.isEmpty() ? "" : "-" + v) + suffix;
        return result;
    }

    /** Return the path fraction: org/the/groupId/artifactId/version */
    public static String toPath(MavenEntityCore coord) {
        String[] gs = coord.getGroupId().split("\\.");

        String g = Arrays.asList(gs).stream()
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .collect(Collectors.joining("/"));

        String result = Arrays.asList(g, coord.getArtifactId(), coord.getVersion()).stream()
                .filter(x -> x != null && !x.isEmpty())
                .collect(Collectors.joining("/"));

        return result;
    }

    public static MavenEntityCore parseId(String mvnIdEx) {
        int remainderOffset = -1;
        for (int i = 0; i < mvnIdEx.length(); ++i) {
            char c = mvnIdEx.charAt(i);
            if (c == '/' || c == '#' || c == '!') {
                remainderOffset = i;
                break;
            }
        }

        String mvnId = remainderOffset == -1
                ? mvnIdEx
                : mvnIdEx.substring(0, remainderOffset);

        String remainder = remainderOffset == -1
                ? null
                : mvnIdEx.substring(remainderOffset);

        String[] tmp = mvnId.split(":", 5);
        String[] parts = new String[5];
        System.arraycopy(tmp, 0, parts, 0, tmp.length);
        return new MavenEntityCoreImpl(parts[0], parts[1], parts[2], parts[3], parts[4], remainder);
    }

    public static MavenEntityCore parseUrn(String mvnUrn) {
        if (!mvnUrn.startsWith(URN_MVN)) {
            throw new IllegalArgumentException("Argument does not start with urn:mvn: - got: " + mvnUrn);
        }

        String arg = mvnUrn.substring(URN_MVN.length());
        MavenEntityCore result = parseId(arg);
        return result;
    }

    /** Parse a maven GAV pattern with colons. The pattern may be optionally prefixed with urn:mvn: */
    public static MavenEntityCore parse(String mvnIdOrUrn) {
        String arg = mvnIdOrUrn.startsWith(URN_MVN)
                ? mvnIdOrUrn.substring(URN_MVN.length())
                : mvnIdOrUrn;

        return parseId(arg);
    }

    public static String toPath(
            MavenEntityCore entity,
            String snapshotPrefix,
            String internalPrefix,
            String componentSeparator,
            boolean includePrefix,
            boolean includeDirectories,
            boolean includeFileName) {
        String result = null;

        boolean isSnapshot = Optional.ofNullable(entity.getVersion())
                .map(String::toUpperCase)
                .map(str -> str.endsWith("-SNAPSHOT")).orElse(false);

        String prefix = includePrefix
                ? (isSnapshot ? snapshotPrefix : internalPrefix)
                : null
                ;

        // Empty prefix is treated as if it was null
        if (prefix != null && prefix.isEmpty()) {
            prefix = null;
        }

        String pathStr = includeDirectories ? toPath(entity) : null;
        String fileName = includeFileName ? toFileName(entity) : null;

        String remainder = entity.getRemainder();
        result = Arrays.asList(prefix, pathStr, fileName).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining(componentSeparator));

        if (remainder != null) {
            result += remainder;
        }

        return result;
    }
}
