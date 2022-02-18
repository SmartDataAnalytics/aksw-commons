package org.aksw.commons.rx.cache.range;

import org.aksw.commons.rx.lookup.ListPaginator;
import org.aksw.commons.util.array.ArrayOps;

import com.google.common.collect.Range;

public class SequentialReaderSourceRx<A>
	implements SequentialReaderSource<A>
{
	protected ArrayOps<A> arrayOps;		
	protected ListPaginator<?> listPaginator;
	
	public SequentialReaderSourceRx(ArrayOps<A> arrayOps, ListPaginator<?> listPaginator) {
		super();
		this.arrayOps = arrayOps;
		this.listPaginator = listPaginator;
	}

	public static <A> SequentialReaderSource<A> create(ArrayOps<A> arrayOps, ListPaginator<?> listPaginator) {
		return new SequentialReaderSourceRx<>(arrayOps, listPaginator);
	}

	@Override
	public SequentialReader<A> newInputStream(Range<Long> range) {
		return new SequentialReaderFromStream<A>(arrayOps, listPaginator.apply(range).blockingStream());
	}
}
