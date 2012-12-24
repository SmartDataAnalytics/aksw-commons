package org.aksw.commons.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/28/11
 *         Time: 10:37 PM
 */
public class StreamUtils {

    public static void copy(InputStream in, OutputStream out, int bufferSize)
            throws IOException {
        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();
    }

    public static void copyThenClose(InputStream in, OutputStream out)
            throws IOException {
        try {
            copy(in, out, 1024);
        } finally {
            in.close();
            out.close();
        }
    }

    public static String toString(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        copyThenClose(in, out);

        return out.toString();
    }

    public static String toStringSafe(InputStream in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            copyThenClose(in, out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return out.toString();
    }
}
