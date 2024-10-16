package org.aksw.commons.io.buffer.plain;

import java.io.IOException;

import org.aksw.commons.io.buffer.array.ArrayOps;

import com.google.common.primitives.Ints;

public class BufferOverArray<A>
    implements Buffer<A>
{
    protected ArrayOps<A> arrayOps;
    protected A array;

    public BufferOverArray(ArrayOps<A> arrayOps, int size) {
        this(arrayOps, arrayOps.create(size));
    }

    public BufferOverArray(ArrayOps<A> arrayOps, A array) {
        this.arrayOps = arrayOps;
        this.array = array;
    }

    public static <A> BufferOverArray<A> create(ArrayOps<A> arrayOps, int size) {
        return new BufferOverArray<>(arrayOps, size);
    }

    public static <A> BufferOverArray<A> create(ArrayOps<A> arrayOps, A array) {
        return new BufferOverArray<>(arrayOps, array);
    }

    @Override
    public void write(long offsetInBuffer, A arrayWithItemsOfTypeT, int arrOffset, int arrLength) {
        int offsetInBufferInt = Ints.checkedCast(offsetInBuffer);
        arrayOps.copy((A)arrayWithItemsOfTypeT, arrOffset, array, offsetInBufferInt, arrLength);
    }

    public A getArray() {
        return array;
    }

    @Override
    public long getCapacity() {
        return arrayOps.length(array);
    }

    @Override
    public long size() throws IOException {
        return arrayOps.length(array);
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return arrayOps;
    }

    @Override
    public int readInto(A tgt, int tgtOffset, long srcOffset, int length) {
        int capacityInt = arrayOps.length(array);
        int srcOffsetInt = Ints.checkedCast(srcOffset);
        int result = Math.max(Math.min(capacityInt - srcOffsetInt, length), 0);
        arrayOps.copy(array, srcOffsetInt, tgt, tgtOffset, result);

        if (result == 0 && length > 0) {
            result = -1;
        }

        return result;
    }

    @Override
    public void put(long offset, Object item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(long index) {
        throw new UnsupportedOperationException();
    }
}
