package org.aksw.commons.util.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Sets;

/**
 * Parse file name patterns of the form baseName.contentType.encoding1.encodingN
 *
 * Limitation: Currently does not support compound contentTypes such as "rdf.xml".
 *   Implementation needs to be revised such that it can "chip away" suffixes of the file name.
 *
 */
public class FileNameParser {
    protected Predicate<String> isContent;
    protected Predicate<String> isEncoding;

    protected FileNameParser(Predicate<String> isContent, Predicate<String> isEncoding) {
        super();
        this.isContent = isContent;
        this.isEncoding = isEncoding;
    }

    public static FileNameParser of(Predicate<String> isContent, Predicate<String> isEncoding) {
        return new FileNameParser(isContent, isEncoding);
    }

    public FileName parse(String filename) {
        List<String> parts = FileNameUtils.deconstruct(filename);

        String content = null;
        List<String> encodings = new ArrayList<>();

        boolean encodingState = true;

        int i;
        for (i = parts.size() - 1; i >= 0; --i) {
            String part = parts.get(i);

            if (encodingState) {
                if (isEncoding.test(part)) {
                    encodings.add(part);
                    continue;
                }

                encodingState = false;
            }

            if (isContent.test(part)) {
                content = part;
            } else {
                // make the current part part of the base name
                ++i;
            }
            break;
        }

        String baseName = FileNameUtils.construct(parts.subList(0, i));
        Collections.reverse(encodings);
        FileName result = new FileNameImpl(baseName, content, encodings);
        return result;
    }

    public static void main(String[] args) {
        Set<String> knownEncodings = Sets.newHashSet("gz", "bz2");
        Set<String> knownContentTypes = Sets.newHashSet("rdf", "xml", "ttl");

        FileNameParser parser = FileNameParser.of(knownEncodings::contains, knownContentTypes::contains);
        FileName fileName = parser.parse("foo.rdf.gz");

        System.out.println(fileName.getBaseName());
        System.out.println(fileName.getContentPart());
        System.out.println(fileName.getEncodingParts());
    }
}
