package org.aksw.commons.io.buffer.array;

import java.util.Arrays;

public class ArrayOpsByte
    implements ArrayOps<byte[]>
{
    // When operations operate on that many items then use the system functions
    public static final int SYSTEM_THRESHOLD = 16;
    public static final Byte ZERO = Byte.valueOf((byte)0);

    @Override
    public Class<?> getArrayClass() {
        return byte[].class;
    }

    @Override
    public byte[] create(int size) {
        try {
            return new byte[size];
        } catch (OutOfMemoryError e) {
            throw new OutOfMemoryError("Failed ot allocate " + size + "bytes");
        }
    }

    @Override
    public Object getDefaultValue() {
        return ZERO;
    }

    public static byte unbox(Object value) {
        Byte boxed = (Byte)value;
        byte result = boxed == null ? (byte)0 : Byte.valueOf(boxed);
        return result;
    }

    @Override
    public Object get(byte[] array, int index) {
        return getByte(array, index);
    }

    @Override
    public void set(byte[] array, int index, Object value) {
        setByte(array, index, unbox(value));
    }

    @Override
    public void fill(byte[] array, int offset, int length, Object value) {
        byte v = unbox(value);

        if (length < SYSTEM_THRESHOLD) {
            for (int i = 0; i < length; ++i) {
                array[offset + i] = v;
            }
        } else {
            Arrays.fill(array, offset, length, v);
        }
    }

    @Override
    public void copy(byte[] src, int srcPos, byte[] dest, int destPos, int length) {
        if (length < SYSTEM_THRESHOLD) {
            int sp = srcPos;
            for (int i = destPos; i < destPos + length; ++i) {
                dest[i] = src[sp++];
            }
        } else {
            System.arraycopy(src, srcPos, dest, destPos, length);
        }
    }

    @Override
    public int length(byte[] array) {
        return array.length;
    }

    @Override
    public byte getByte(byte[] array, int index) {
        return array[index];
    }

    @Override
    public void setByte(byte[] array, int index, byte value) {
        array[index] = value;
    }
}
