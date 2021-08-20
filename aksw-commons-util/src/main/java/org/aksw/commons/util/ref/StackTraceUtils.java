package org.aksw.commons.util.ref;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class StackTraceUtils {

    public static final boolean IS_ASSERT_ENABLED = isAssertEnabled();

    public static boolean isAssertEnabled() {
        boolean result;
        try {
           assert false;
           result = false;
        } catch (AssertionError e) {
           result = true;
        }
        return result;
    }

    public static StackTraceElement[] getStackTraceIfEnabled() {
        StackTraceElement[] result = IS_ASSERT_ENABLED
                ? Thread.currentThread().getStackTrace()
                : null;

        return result;
    }


    public static String toString(StackTraceElement[] stackTrace) {
        String result = stackTrace == null
                ? "(stack traces not enabled - enable assertions using the -ea jvm option)"
                : Arrays.asList(stackTrace).stream().map(s -> "  " + Objects.toString(s))
                    .collect(Collectors.joining("\n"));

        return result;
    }

}
