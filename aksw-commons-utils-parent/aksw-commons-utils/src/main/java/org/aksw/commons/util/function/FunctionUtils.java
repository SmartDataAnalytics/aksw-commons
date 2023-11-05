package org.aksw.commons.util.function;

import java.util.function.Function;

public class FunctionUtils {
    /** Wrap a function such that whenever it would return null it would return the argument instead */
    public static <O, I extends O> Function<I, O> nullToIdentity(Function<I, O> fn) {
        return i -> {
            O o = fn.apply(i);
            O r = o == null ? i : o;
            return r;
        };
    }
}
