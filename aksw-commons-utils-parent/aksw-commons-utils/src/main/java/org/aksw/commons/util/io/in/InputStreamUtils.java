package org.aksw.commons.util.io.in;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class InputStreamUtils {
    /** If the given stream does not support marks then returns it wrapped with a buffered input stream. */
    public static InputStream forceMarkSupport(InputStream in) {
        InputStream result = in.markSupported()
                ? in
                : new BufferedInputStream(in);
        return result;
    }

}
