package org.aksw.commons.util.array;

import java.util.Arrays;

public class ArrayOpsObject
	implements ArrayOps<Object[]>
{
	// When operations operate on that many items then use the system functions
	public static final int SYSTEM_THRESHOLD = 16;


	@Override
	public Object[] create(int size) {
		return new Object[size];
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
