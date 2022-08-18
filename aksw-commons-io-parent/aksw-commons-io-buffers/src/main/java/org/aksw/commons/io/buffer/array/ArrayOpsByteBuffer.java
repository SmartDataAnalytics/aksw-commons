package org.aksw.commons.io.buffer.array;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.google.common.base.Preconditions;

public class ArrayOpsByteBuffer
    implements ArrayOps<ByteBuffer>
{
    @Override
    public Class<?> getArrayClass() {
        return ByteBuffer.class;
    }

    @Override
    public ByteBuffer create(int size) {
        return ByteBuffer.allocate(size);
    }

    @Override
    public Object get(ByteBuffer buffer, int index) {
        return buffer.get(index);
    }

    @Override
    public void set(ByteBuffer buffer, int index, Object value) {
        byte b = ((Byte)value).byteValue();
        buffer.put(index, b);
    }

    @Override
    public int length(ByteBuffer buffer) {
        return buffer.remaining();
    }

    /** Absolute fill operation (does not change the position of the buffer) */
    @Override
    public void fill(ByteBuffer buffer, int offset, int length, Object value) {
        if (buffer.hasArray()) {
            byte[] array = buffer.array();
            int effectiveOffset = buffer.arrayOffset() + buffer.position() + offset;

            ArrayOps.BYTE.fill(array, effectiveOffset, length, value);
        } else {
            byte b = ((Byte)value).byteValue();

            int p = buffer.position() + offset;
            if (length < ArrayOpsByte.SYSTEM_THRESHOLD) {
                for (int i = p; i < p + length; ++i) {
                    buffer.put(i, b);
                }
            } else {
                byte[] tmp = new byte[length];
                if (b != 0) {
                    Arrays.fill(tmp, b);
                }
                ByteBuffer dup = buffer.duplicate();
                dup.position(p);
                dup.put(tmp);
            }
        }
    }

    /** Absolute copy operation (does not change the positions of the involved buffers) */
    @Override
    public void copy(ByteBuffer src, int srcPos, ByteBuffer dest, int destPos, int length) {
        Preconditions.checkArgument(dest.remaining() >= length, "Insufficient capacity of destination");

        int start = src.position() + srcPos;
        int end = start + length;
        ByteBuffer srcDup = src.duplicate();
        srcDup.position(start);
        srcDup.limit(end);

        ByteBuffer dstDup = dest.duplicate();
        dstDup.position(dest.position() + destPos);
        dstDup.put(srcDup);
    }

    @Override
    public Object getDefaultValue() {
        return Byte.valueOf((byte)0);
    }

}
