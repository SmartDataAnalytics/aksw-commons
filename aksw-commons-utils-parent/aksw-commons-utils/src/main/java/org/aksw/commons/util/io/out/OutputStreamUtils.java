package org.aksw.commons.util.io.out;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class OutputStreamUtils {
    public static String toStringUtf8(Consumer<OutputStream> action, Charset charset) {
        return toString(action, StandardCharsets.UTF_8);
    }

    public static String toString(Consumer<OutputStream> action, Charset charset) {
        String result;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            action.accept(out);
            byte[] bytes = out.toByteArray();
            result = new String(bytes, charset);
        } catch (IOException e) {
            throw new RuntimeException(e); // Should never happen
        }
        return result;
    }
}
