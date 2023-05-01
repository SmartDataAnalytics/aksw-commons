package org.aksw.commons.util.string;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.commons.util.entity.EntityInfo;

import com.google.common.collect.Streams;

public class FileNameUtils {
    
    /** Decompose a filename into parts by splitting by dot ('.') */
    public static List<String> deconstruct(String filename) {
        List<String> result = Arrays.asList(filename.split("\\."));
        return result;
    }
    
    /** Join strings with dot ('.') to form a filename; discards parts that are empty strings (causes consecutive dots to collapse into a single dot) */
    public static String construct(Iterable<String> parts) {
        String result = Streams.stream(parts)
        		.filter(str -> !str.isEmpty())
        		.collect(Collectors.joining("."));
        return result;
    }


    /**
     * Removes all encoding parts from the filename; optionally also removes the content type part.
     *
     * Given a file name and its detected encodings and content type,
     * assume that the base name can be obtained by removing that many trailing file extensions.
     *
     * For example, if for the file foo.bar.tar.gz the content type tar and encoding gz were
     * recognized then 2 trailing parts will be removed and the base name becomes foo.bar.
     *
     * At present the removal does not check whether the trailing parts actually correspond
     * to the detected encodings / content type - so if foo.bar.x.y is also probed to a gzipped tar
     * then the base name is also foo.bar.
     *
     * @param filename
     * @param entityInfo
     * @return
     */
    public static FileName deriveFileName(String filename, EntityInfo entityInfo) {
        String baseName;
        String contentPart;
        List<String> encodingParts;

        List<String> parts = FileNameUtils.deconstruct(filename);
        int n = parts.size();
        
        // If besides encodings there are parts for base name(s) and content type then...
        int numEncodings = entityInfo.getContentEncodings().size();
        if (parts.size() >= numEncodings + 2) {
            int offset = n - numEncodings;
        	encodingParts = parts.subList(offset--, n);
            contentPart = parts.get(offset);
            baseName = construct(parts.subList(0, offset));
        } else {
        	// There are insufficient parts - ignore encodings and optionally try to derive a contentPart
        	encodingParts = Collections.emptyList();
        	baseName = n >= 1 ? parts.get(0) : "";
        	contentPart = n >= 2 ? parts.get(1) : "";
        }

        FileName result = FileNameImpl.create(baseName, contentPart, encodingParts);
        return result;
    }

}
