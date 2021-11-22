package org.aksw.commons.rx.cache.range;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.aksw.commons.rx.lookup.ListPaginator;
import org.aksw.commons.rx.lookup.ListPaginatorFromList;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;

public abstract class RangeCacheTestSuite {

    private static final Logger logger = LoggerFactory.getLogger(RangeCacheTestSuite.class);

    protected abstract <T> ListPaginator<T> wrapWithCache(String testId, ListPaginator<T> backend);

    @Test
    public void test() throws InterruptedException {

        List<String> items = LongStream.range(0, 10000)
                .mapToObj(i -> "item #" + i)
                .collect(Collectors.toList());

        ListPaginator<String> backend = ListPaginatorFromList.wrap(items);
//        ListPaginator<String> frontend = backend;
        ListPaginator<String> frontend = wrapWithCache("test", backend);

//        Flowable<String> flow = frontend.apply(Range.closedOpen(10l, 20l));
        Flowable<String> flow = frontend.apply(Range.closedOpen(10l, 2000l));

        // System.out.println(flow.toList().blockingGet());
        List<String> actual = flow.toList().blockingGet();

        System.out.println(actual);

        // flow.toList().blockingGet().forEach(System.out::println);

        Thread.sleep(3000);

        for (int i = 0; i < 3; ++i) {
            System.gc();
        }

        logger.info("Done waiting");
    }


    // @Test
    public void testCount() throws InterruptedException {

        List<String> items = LongStream.range(0, 10000)
                .mapToObj(i -> "item #" + i)
                .collect(Collectors.toList());

        ListPaginator<String> backend = ListPaginatorFromList.wrap(items);
        // ListPaginator<String> frontend = backend;
        ListPaginator<String> frontend = wrapWithCache("testCount", backend);


        Range<Long> range = frontend.fetchCount(null, null).blockingGet();

        System.out.println(range);

        Thread.sleep(3000);
        System.gc();
        logger.info("Done waiting");
    }


}
