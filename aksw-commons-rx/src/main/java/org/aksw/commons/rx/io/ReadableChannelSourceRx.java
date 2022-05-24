package org.aksw.commons.rx.io;

import java.util.stream.Stream;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.input.ReadableChannel;
import org.aksw.commons.io.input.ReadableChannelOverStream;
import org.aksw.commons.io.input.ReadableChannelSource;
import org.aksw.commons.rx.lookup.ListPaginator;
import org.aksw.commons.util.range.CountInfo;
import org.aksw.commons.util.range.RangeUtils;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;

public class ReadableChannelSourceRx<T>
    implements ReadableChannelSource<T[]>
{
    protected ArrayOps<T[]> arrayOps;
    protected ListPaginator<T> listPaginator;

    public ReadableChannelSourceRx(ArrayOps<T[]> arrayOps, ListPaginator<T> listPaginator) {
        super();
        this.arrayOps = arrayOps;
        this.listPaginator = listPaginator;
    }

    @Override
    public ArrayOps<T[]> getArrayOps() {
        return arrayOps;
    }

    public static <T> ReadableChannelSource<T[]> create(ArrayOps<T[]> arrayOps, ListPaginator<T> listPaginator) {
        return new ReadableChannelSourceRx<>(arrayOps, listPaginator);
    }

    @Override
    public ReadableChannel<T[]> newReadableChannel(Range<Long> range) {
    	Flowable<T> flowable = listPaginator.apply(range);
		Stream<T> stream = flowable.blockingStream();
        return new ReadableChannelOverStream<T>(arrayOps, stream);
    }

    @Override
    public long size() {
        Range<Long> tmp = listPaginator.fetchCount(null, null).blockingGet();
        CountInfo ci = RangeUtils.toCountInfo(tmp);
        long result = ci.isHasMoreItems() ? -1 : ci.getCount();
        return result;
    }
}
