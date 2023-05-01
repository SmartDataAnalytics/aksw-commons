package org.aksw.commons.io.buffer.array;

import java.lang.reflect.Array;
import java.util.function.IntFunction;

public interface ArrayOps<A> {
    /** Return the array class (if known) - such as byte[].class */
    Class<?> getArrayClass();

    A create(int size);

    Object get(A array, int index);
    void set(A array, int index, Object value);

    int length(A array);

    void fill(A array, int offset, int length, Object value);
    void copy(A src, int srcPos, A dest, int destPos, int length);
    Object getDefaultValue();

    @SuppressWarnings("unchecked")
    default void fillRaw(Object array, int offset, int length, Object value) {
        fill((A)array, offset, length, value);
    }

    @SuppressWarnings("unchecked")
    default void copyRaw(Object src, int srcPos, Object dest, int destPos, int length) {
        copy((A)src, srcPos, (A)dest, destPos, length);
    }

    @SuppressWarnings("unchecked")
    default Object getRaw(Object array, int index) {
        return get((A)array, index);
    }

    @SuppressWarnings("unchecked")
    default void setRaw(Object array, int index, Object value) {
        set((A)array, index, value);
    }

    @SuppressWarnings("unchecked")
    default void lengthRaw(Object array) {
        length((A)array);
    }

    // TODO Cache with a ClassInstanceMap?
    @SuppressWarnings("unchecked")
    public static <T> ArrayOpsObject<T> createFor(Class<T> componentType) {
        return new ArrayOpsObject<>(size -> (T[])Array.newInstance(componentType, size));
    }

    public static <T> ArrayOpsObject<T> createFor(IntFunction<T[]> arrayConstructor) {
        return new ArrayOpsObject<>(arrayConstructor);
    }


    public static final ArrayOpsByte BYTE = new ArrayOpsByte();
    public static final ArrayOpsByteBuffer BYTE_BUFFER = new ArrayOpsByteBuffer();
    public static final ArrayOpsObject<Object> OBJECT = createFor(Object.class);

    public static <T> ArrayOps<T[]> forObjects() {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        ArrayOps<T[]> result = (ArrayOps)ArrayOps.OBJECT;
        return result;
    }

    /** Experimental primitive access methods. May improve single item operation performance
     * at the expense of somewhat cluttering up the API.  */

    default byte getByte(A array, int index) {
        return (byte)get(array, index);
    }

    default void setByte(A array, int index, byte value) {
        set(array, index, value);
    }

    @SuppressWarnings("unchecked")
    default byte getByteRaw(Object array, int index) {
        return getByte((A)array, index);
    }

    @SuppressWarnings("unchecked")
    default void setByteRaw(Object array, int index, byte value) {
        setByte((A)array, index, value);
    }
}
