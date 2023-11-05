package org.aksw.commons.util.obj;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.ClassUtils;

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
     * Check if the given object can be assigned to given class.
     * Also works for primitive types, e.g. int can be assigned to Long.
     *
     * @implNote
     *   Relies on {@link ClassUtils#isAssignable(Class, Class)}
     */
    public static boolean canCastAs(Class<?> clazz, Object o) {
        boolean result = o == null ? true : ClassUtils.isAssignable(o.getClass(), clazz);
        return result;
    }

    public static <T> Optional<T> tryCastAs(Class<T> clazz, Object o) {
        boolean canCastAs = canCastAs(clazz, o);
        Optional<T> result = canCastAs ? Optional.of(castAs(clazz, o)) : Optional.empty();
        return result;
    }

    public static <T> T castAs(Class<T> clazz, Object o) {
        @SuppressWarnings("unchecked")
        T result = (T)o;
        return result;
    }

    public static <T> T castAsOrNull(Class<T> clazz, Object o) {
        T result = canCastAs(clazz, o) ? castAs(clazz, o) : null;
        return result;
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
