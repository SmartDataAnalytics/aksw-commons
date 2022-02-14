package org.aksw.commons.rx.cache.range;

import java.nio.ByteBuffer;

import org.aksw.commons.util.array.ArrayWritable;

public interface ByteArrayPuttable
	extends ArrayWritable
{
	void putBytes(long offsetInBuffer, ByteBuffer byteBuffer);
	
	default void putBytes(long offsetInBuffer, byte[] byteArray, int arrOffset, int arrLength) {
		ByteBuffer bb = ByteBuffer.wrap(byteArray, arrOffset, arrLength);
		putBytes(offsetInBuffer, bb);
	}
	
	@Override
	default void putAll(long offsetInBuffer, Object arrayWithItemsOfTypeT, int arrOffset, int arrLength) {
		byte[] bytes = (byte[])arrayWithItemsOfTypeT;
		putBytes(offsetInBuffer, bytes, arrOffset, arrLength);
	}
}
