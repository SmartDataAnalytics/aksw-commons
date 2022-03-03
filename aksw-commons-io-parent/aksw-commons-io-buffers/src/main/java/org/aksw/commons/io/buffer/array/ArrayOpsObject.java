package org.aksw.commons.io.buffer.array;

import java.util.Arrays;
import java.util.function.IntFunction;

public class ArrayOpsObject<T>
	implements ArrayOps<T[]>
{
	// When operations operate on that many items then use the system functions
	public static final int SYSTEM_THRESHOLD = 16;
	protected IntFunction<T[]> arrayConstructor;

	public ArrayOpsObject(IntFunction<T[]> arrayConstructor) {
		super();
		this.arrayConstructor = arrayConstructor;
	}
	
	@Override
	public T[] create(int size) {
		return arrayConstructor.apply(size);
	}

	@Override
	public Object getDefaultValue() {
		return null;
	}

	@Override
	public Object get(Object[] array, int index) {
		return array[index];
	}
	
	@Override
	public void set(Object[] array, int index, Object value) {
		array[index] = value;
	}
	
	@Override
	public void fill(Object[] array, int offset, int length, Object value) {
		if (length < SYSTEM_THRESHOLD) {
			for (int i = 0; i < length; ++i) {
				array[offset + i] = value;
			}			
		} else {
			Arrays.fill(array, offset, length, value);
		}
	}

	@Override
	public void copy(Object[] src, int srcPos, Object[] dest, int destPos, int length) {
		if (length < SYSTEM_THRESHOLD) {
			for (int i = 0; i < length; ++i) {
				dest[destPos + i] = src[srcPos + i];
			}
		} else {
			System.arraycopy(src, srcPos, dest, destPos, length);
		}
	}

	@Override
	public int length(Object[] array) {
		return array.length;
	}
}
