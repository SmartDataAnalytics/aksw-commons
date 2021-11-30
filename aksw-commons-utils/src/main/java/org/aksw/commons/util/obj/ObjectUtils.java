package org.aksw.commons.util.obj;

public class ObjectUtils {
    /** For a given object derive a string of the form className@identityHashCode */
    public static String toStringWithIdentityHashCode(Object obj) {
        return toStringWithIdentityHashCode(obj, "(null)");
    }

    /** For a given object derive a string of the form className@identityHashCode */
    public static String toStringWithIdentityHashCode(Object obj, String nullDefault) {
        return obj != null ? obj.getClass().getName() + "@" + System.identityHashCode(obj) : nullDefault;
    }
}
