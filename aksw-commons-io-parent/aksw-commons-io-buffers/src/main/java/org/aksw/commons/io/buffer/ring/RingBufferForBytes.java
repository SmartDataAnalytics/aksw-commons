package org.aksw.commons.io.buffer.ring;

import org.aksw.commons.io.buffer.array.ArrayOps;

/** Adapter of {@link RingBufferBase} for bytes. */
public class RingBufferForBytes
    extends RingBufferBase<byte[]>
{
    public RingBufferForBytes(int size) {
        super(new byte[size]);
    }

    protected RingBufferForBytes(byte[] data, int start, int end, boolean isEmpty) {
        super(data, start, end, isEmpty);
    }

    public byte get(int offset) {
        int pos = start + offset;
        if (pos >= bufferLen) {
            pos -= bufferLen;
        }

        byte result = buffer[pos];
        return result;
    }

    @Override
    public ArrayOps<byte[]> getArrayOps() {
        return ArrayOps.BYTE;
    }

    /**
     * Returns a new instance with the same data but a copy of the start and end pointers.
     * The purpose is to support reading data from the buffer and eventually reset the state.
     */
    // XXX Likely mark() and reset() would be the cleaner approach.
    public RingBufferForBytes shallowClone() {
        return new RingBufferForBytes(buffer, start, end, isEmpty);
    }
}
