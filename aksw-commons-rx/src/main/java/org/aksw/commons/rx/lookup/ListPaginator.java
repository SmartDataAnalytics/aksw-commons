package org.aksw.commons.rx.lookup;

import java.util.List;
import java.util.function.Function;

import org.aksw.commons.rx.range.RangedSupplier;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public interface ListPaginator<T>
    extends RangedSupplier<Long, T>
{
    Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit);

    default List<T> fetchList(Range<Long> range) {
        List<T> result = apply(range).toList().blockingGet(); //.collect(Collectors.toList());
        return result;
    }

    default ListPaginator<List<T>> batch(long chunkSize) {
        return new ListPaginatorBatch<T>(this, chunkSize);
    }


    default <U> ListPaginator<U> map(Function<? super T, U> mapper) {
        ListPaginator<T> backend = this;

        return new ListPaginator<U>() {
            @Override
            public Flowable<U> apply(Range<Long> t) {
                return backend.apply(t).map(mapper::apply);
            }

            @Override
            public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
                return backend.fetchCount(itemLimit, rowLimit);
            }
        };
    }
}
