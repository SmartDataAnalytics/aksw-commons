package org.aksw.commons.rx.cache.range;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

import org.aksw.commons.util.array.ArrayOps;
import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;

public class SequentialReaderFromStream<A>
	extends AutoCloseableWithLeakDetectionBase
	implements SequentialReader<A>
{
	protected ArrayOps<A> arrayOps;		
	protected Stream<?> stream = null;
	protected Iterator<?> iterator = null;

	public SequentialReaderFromStream(ArrayOps<A> arrayOps, Stream<?> stream) {
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
	public int read(A array, int position, int length) throws IOException {
		ensureOpen();
		
		int i;
		for (i = 0; i < length && iterator.hasNext(); ++i) {
			Object value = iterator.next();
			arrayOps.set(array, position + i, value);
		}
		
		return i == 0 ? -1 : i;
	}
}