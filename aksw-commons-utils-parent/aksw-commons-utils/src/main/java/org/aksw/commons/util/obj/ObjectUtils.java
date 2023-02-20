package org.aksw.commons.util.obj;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

public class ObjectUtils {

    /** Return an object's class - returns null for a null argument */
    public static Class<?> getClass(Object object) {
        return object == null ? null : object.getClass();
    }

    /** For a given object derive a string of the form className@identityHashCode */
    public static String toStringWithIdentityHashCode(Object obj) {
        return toStringWithIdentityHashCode(obj, "(null)");
    }

    /** For a given object derive a string of the form className@identityHashCode */
    public static String toStringWithIdentityHashCode(Object obj, String nullDefault) {
        return obj != null ? obj.getClass().getName() + "@" + System.identityHashCode(obj) : nullDefault;
    }

    /**
     * Supplier-based coalesce function as described in
     * https://benjiweber.co.uk/blog/2013/12/08/null-coalescing-in-java-8/
     *
     * <br />
     * Example usage:
     * {@code coalesce(obj::getName, obj::getId, () -> obj.hasFoo() ? obj.foo().toString() : "bar") }
     *
     *
     * @param <T>
     * @param ts
     * @return
     */
    @SafeVarargs
    public static <T> T coalesce(Supplier<T>... ts) {
        return Arrays.asList(ts)
                .stream()
                .map(t -> t.get())
                .filter(t -> t != null)
                .findFirst()
                .orElse(null);
    }

    /** A consistency check utility. If both given values are non-null then they must be equal otherwise an exception is raised.
     * Returns the first non-null value (if present) otherwise null. */
    public static <T> T requireNullOrEqual(T a, T b) {
        T result;

        boolean isInconsistent = a != null && b != null && !Objects.equals(a, b);
        if (isInconsistent) {
            throw new IllegalArgumentException(String.format("Arguments %s and %s must both be equal or one must be null", a, b));
        } else {
            result = a == null ? b : a;
        }

        return result;
    }
}
