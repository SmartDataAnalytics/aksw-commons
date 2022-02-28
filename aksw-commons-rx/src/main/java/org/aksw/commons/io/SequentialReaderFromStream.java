package org.aksw.commons.io;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

import org.aksw.commons.util.array.ArrayOps;
import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;

public class SequentialReaderFromStream<T>
	extends AutoCloseableWithLeakDetectionBase
	implements SequentialReader<T[]>
{
	protected ArrayOps<T[]> arrayOps;		
	protected Stream<T> stream = null;
	protected Iterator<T> iterator = null;

	public SequentialReaderFromStream(ArrayOps<T[]> arrayOps, Stream<T> stream) {
		super();
		this.arrayOps = arrayOps;
		this.stream = stream;
		this.iterator = stream.iterator();
	}
	@Override
	public void closeActual() throws IOException {
		if (stream != null) {
			stream.close();
		}
	}

	@Override
	public int read(T[] array, int position, int length) throws IOException {
		ensureOpen();
		
		int i;
		for (i = 0; i < length && iterator.hasNext(); ++i) {
			Object value = iterator.next();
			arrayOps.set(array, position + i, value);
		}
		
		// return 0 if length is 0
		int result = i == 0 && length > 0 ? -1 : i;
		return result;
	}
}