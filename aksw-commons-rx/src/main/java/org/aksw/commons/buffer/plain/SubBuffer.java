package org.aksw.commons.buffer.plain;

import java.io.IOException;

import org.aksw.commons.util.array.ArrayOps;

import com.google.common.math.LongMath;

public class SubBuffer<A>
	implements Buffer<A>
{
	protected Buffer<A> backend;
	protected long start;
	protected long length;
	
	public SubBuffer(Buffer<A> backend, long start, long length) {
		super();
		this.backend = backend;
		this.start = start;
		this.length = length;
	}

	@Override
	public void write(long offsetInBuffer, A arrayWithItemsOfTypeT, int arrOffset, int arrLength) throws IOException {
		if (offsetInBuffer - start + arrLength > length) {
			throw new RuntimeException("Attempt to read beyond buffer capacity");
		}
		
		backend.write(LongMath.checkedAdd(start, offsetInBuffer), arrayWithItemsOfTypeT, arrOffset, arrLength);
	}

	@Override
	public int readInto(A tgt, int tgtOffset, long srcOffset, int length) throws IOException {
		return backend.readInto(tgt, tgtOffset, LongMath.checkedAdd(start, srcOffset), length);
	}

	@Override
	public long getCapacity() {
		return length;
	}

	
	@Override
	public ArrayOps<A> getArrayOps() {
		return backend.getArrayOps();
	}
	
	@Override
	public Buffer<A> slice(long offset, long length) {
		throw new UnsupportedOperationException();	
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

