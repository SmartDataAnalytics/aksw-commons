package org.aksw.commons.rx.io;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.input.SequentialReader;
import org.aksw.commons.io.input.SequentialReaderFromStream;
import org.aksw.commons.io.input.SequentialReaderSource;
import org.aksw.commons.rx.lookup.ListPaginator;

import com.google.common.collect.Range;

public class SequentialReaderSourceRx<T>
	implements SequentialReaderSource<T[]>
{
	protected ArrayOps<T[]> arrayOps;		
	protected ListPaginator<T> listPaginator;
	
	public SequentialReaderSourceRx(ArrayOps<T[]> arrayOps, ListPaginator<T> listPaginator) {
		super();
		this.arrayOps = arrayOps;
		this.listPaginator = listPaginator;
	}

	public static <T> SequentialReaderSource<T[]> create(ArrayOps<T[]> arrayOps, ListPaginator<T> listPaginator) {
		return new SequentialReaderSourceRx<>(arrayOps, listPaginator);
	}

	@Override
	public SequentialReader<T[]> newInputStream(Range<Long> range) {
		return new SequentialReaderFromStream<T>(arrayOps, listPaginator.apply(range).blockingStream());
	}
}
