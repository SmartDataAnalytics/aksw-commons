package org.aksw.commons.rx.cache.range;

import org.aksw.commons.util.array.ArrayOps;

public class MainPlaygroundBuffers {
	public static void main(String[] args) {
		PagedBuffer<byte[]> buffer = PagedBuffer.create(ArrayOps.BYTE, 16);
		
		for (int i = 0; i < 32 ;++i) {
			buffer.put(i, (byte)('a' + i));
		}


		for (int i = 0; i < 32 ;++i) {
			System.out.println(buffer.get(i));
		}

		System.out.println("Map: " + buffer.getPageMap().size());
		
	}
}
