package org.aksw.commons.util;

import org.coode.owlapi.obo.renderer.OBORelationshipGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/28/11
 *         Time: 10:37 PM
 */
public class StreamUtils {
    public static String toString(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // FIXME Maybe add the code to aksw-commons
        org.apache.log4j.lf5.util.StreamUtils.copyThenClose(in, out);
        
        return out.toString();
    }

    public static String toStringSafe(InputStream in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // FIXME Maybe add the code to aksw-commons
        try {
            org.apache.log4j.lf5.util.StreamUtils.copyThenClose(in, out);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        
        return out.toString();
    }
}
