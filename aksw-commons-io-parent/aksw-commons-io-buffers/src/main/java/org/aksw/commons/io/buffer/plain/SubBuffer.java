package org.aksw.commons.io.buffer.plain;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;

import com.google.common.math.LongMath;

public interface SubBuffer<A>
    extends Buffer<A>
{
    Buffer<A> getBackend();
    long getStart();
    long getLength();

    @Override
    default void write(long offsetInBuffer, A arrayWithItemsOfTypeT, int arrOffset, int arrLength) throws IOException {
        Buffer<A> backend = getBackend();
        long start = getStart();
        long length = getCapacity();
        if (offsetInBuffer - start + arrLength > length) {
            throw new RuntimeException("Attempt to read beyond buffer capacity");
        }

        backend.write(LongMath.checkedAdd(start, offsetInBuffer), arrayWithItemsOfTypeT, arrOffset, arrLength);
    }

    @Override
    default int readInto(A tgt, int tgtOffset, long srcOffset, int length) throws IOException {
        Buffer<A> backend = getBackend();
        long start = getStart();
        long subLen = getLength();

        long s = LongMath.checkedAdd(start, srcOffset);
        long maxLength = srcOffset >= subLen ? 0 : subLen - srcOffset;
        int l = (int)Math.min(length, maxLength);
        return backend.readInto(tgt, tgtOffset, s, l);
    }

    @Override
    default long getCapacity() {
        Buffer<A> backend = getBackend();
        long start = getStart();
        long length = getLength();
        // Cap the backend capacity by the length
        long result = Math.max(0, Math.min(backend.getCapacity() - start, length));
        return result;
    }



    @Override
    default ArrayOps<A> getArrayOps() {
        Buffer<A> backend = getBackend();
        return backend.getArrayOps();
    }

    @Override
    default Buffer<A> slice(long offset, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void put(long offset, Object item) {
        throw new UnsupportedOperationException();
    }

    @Override
    default Object get(long index) {
        throw new UnsupportedOperationException();
    }
}