package org.aksw.commons.rx.cache.range;

import java.util.Iterator;

import org.aksw.commons.cache.range.AdvancedRangeCacheImpl;
import org.aksw.commons.collection.slice.SliceMetaDataBasic;
import org.aksw.commons.io.SequentialReaderIterator;
import org.aksw.commons.io.SequentialReaderSource;
import org.aksw.commons.rx.io.SequentialReaderSourceRx;
import org.aksw.commons.rx.lookup.ListPaginator;
import org.aksw.commons.rx.util.FlowableUtils;
import org.aksw.commons.util.array.ArrayOps;
import org.aksw.commons.util.range.CountInfo;
import org.aksw.commons.util.range.RangeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Caching list paginator implementation based on {@link AdvancedRangeCacheImpl}
 *
 * @author raven
 *
 * @param <T>
 */
public class ListPaginatorCache<T>
    implements ListPaginator<T>
{
    // private static final Logger logger = LoggerFactory.getLogger(SmartRangeCacheNew.class);

    /** The supplier for actually retrieving data from the backend */
    protected ListPaginator<T> backend;
    protected AdvancedRangeCacheImpl<T[]> core;
    protected Single<Range<Long>> countSingle;

    public ListPaginatorCache(
            ListPaginator<T> backend,
            AdvancedRangeCacheImpl.Builder<T[]> cacheBuilder) {
        this.backend = backend;

        ArrayOps<T[]> arrayOps = cacheBuilder.getSlice().getArrayOps();
        SequentialReaderSource<T[]> source = SequentialReaderSourceRx.create(arrayOps, backend);
        cacheBuilder.setDataSource(source);
        core = cacheBuilder.build();

        this.countSingle = backend
                .fetchCount(null, null)
                .map(r -> {
                    CountInfo countInfo = RangeUtils.toCountInfo(r);
                    if (!countInfo.isHasMoreItems()) {
                        long count = countInfo.getCount();
                        core.getSlice().mutateMetaData(metaData -> metaData.setKnownSize(count));
                    }
                    return r;
                })
                .cache();
    }


    public static <T> ListPaginatorCache<T> create(
            ListPaginator<T> backend,
            AdvancedRangeCacheImpl.Builder<T[]> builder) {
        return new ListPaginatorCache<>(backend, builder);
    }

    public AdvancedRangeCacheImpl<T[]> getCore() {
        return core;
    }

    @Override
    public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {

        Single<Range<Long>> result;

        long knownSize = core.getSlice().computeFromMetaData(false, SliceMetaDataBasic::getKnownSize);


        if (knownSize >= 0) {
            result = Single.just(Range.singleton(knownSize));
        } else {
            result = countSingle;
        }

        return result;
    }

    @Override
    public Flowable<T> apply(Range<Long> range) {
        return adapt(core.getSlice().getArrayOps(), core, range);
    }


    public static <T> Flowable<T> adapt(ArrayOps<T[]> arrayOps, SequentialReaderSource<T[]> source, Range<Long> range) {
        return FlowableUtils.createFlowableFromResource(
                () -> source.newInputStream(range),
                in -> SequentialReaderIterator.create(arrayOps, in),
                Iterator::hasNext,
                Iterator::next,
                t -> {
                    try {
                        t.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

}
