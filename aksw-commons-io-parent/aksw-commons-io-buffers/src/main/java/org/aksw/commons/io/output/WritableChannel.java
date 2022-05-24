package org.aksw.commons.io.output;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

import org.aksw.commons.io.buffer.array.HasArrayOps;

public interface WritableChannel<A>
	extends HasArrayOps<A>, Flushable, Closeable
{
	boolean isOpen();

	/**
	 * Write method following the usual OutputStream protocol.
	 *
	 * @param array The array from which to obtain the data to write
	 * @param position Position in the array from which to start reading
	 * @param length Maximum number of items to read from the array.
	 *
	 * @throws IOException
	 */
	void write(A array, int position, int length) throws IOException;

	@SuppressWarnings("unchecked")
	default void writeRaw(Object array, int position, int length) throws IOException {
		write((A)array, position, length);
	}
}
