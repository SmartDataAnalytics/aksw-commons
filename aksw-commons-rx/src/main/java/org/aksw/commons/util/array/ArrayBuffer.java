package org.aksw.commons.util.array;

import org.aksw.commons.buffer.plain.Buffer;

import com.google.common.primitives.Ints;

public class ArrayBuffer<A>
	implements Buffer<A>
{
	protected ArrayOps<A> arrayOps;
	protected A array;

	public ArrayBuffer(ArrayOps<A> arrayOps, int size) {
		this(arrayOps, arrayOps.create(size));
	}

	public ArrayBuffer(ArrayOps<A> arrayOps, A array) {
		this.arrayOps = arrayOps;
		this.array = array;
	}
	
	public static <A> ArrayBuffer<A> create(ArrayOps<A> arrayOps, int size) {
		return new ArrayBuffer<>(arrayOps, size);
	}

	public static <A> ArrayBuffer<A> create(ArrayOps<A> arrayOps, A array) {
		return new ArrayBuffer<>(arrayOps, array);
	}

	@Override
	public void write(long offsetInBuffer, A arrayWithItemsOfTypeT, int arrOffset, int arrLength) {
		int offsetInBufferInt = Ints.checkedCast(offsetInBuffer);
		arrayOps.copy((A)arrayWithItemsOfTypeT, arrOffset, array, offsetInBufferInt, arrLength);
	}

	@Override
	public long getCapacity() {
		return arrayOps.length(array);
	}

	@Override
	public ArrayOps<A> getArrayOps() {
		return arrayOps;
	}
	
	@Override
	public int readInto(A tgt, int tgtOffset, long srcOffset, int length) {
		int srcOffsetInt = Ints.checkedCast(srcOffset);
		arrayOps.copy(array, srcOffsetInt, tgt, tgtOffset, length);
		return length;
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
