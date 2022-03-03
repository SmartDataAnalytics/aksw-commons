package org.aksw.commons.rx.cache.range;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.buffer.plain.PagedBuffer;
import org.aksw.commons.io.cache.AdvancedRangeCacheImpl;
import org.aksw.commons.io.cache.AdvancedRangeCacheImpl.Builder;
import org.aksw.commons.io.slice.Slice;
import org.aksw.commons.io.slice.SliceInMemory;
import org.aksw.commons.io.slice.SliceWithPagesSyncToDisk;
import org.aksw.commons.path.core.PathOpsStr;
import org.aksw.commons.rx.lookup.ListPaginator;
import org.aksw.commons.rx.lookup.ListPaginatorFromList;
import org.aksw.commons.store.object.key.api.ObjectStore;
import org.aksw.commons.store.object.key.impl.KryoUtils;
import org.aksw.commons.store.object.key.impl.ObjectStoreImpl;
import org.aksw.commons.store.object.path.impl.ObjectSerializerKryo;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.common.base.Stopwatch;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class TestListPaginatorCache {

    private static final Logger logger = LoggerFactory.getLogger(TestListPaginatorCache.class);

    public static List<String> createTestList(int size) {
        return new AbstractList<String>() {

            @Override
            public String get(int index) {
                if (index < 0 || index >= size) {
                    throw new IndexOutOfBoundsException();
                }

                return "item #" + index;
            }

            @Override
            public int size() {
                return size;
            }
        };
    }

    public static ListPaginator<String> createListWithRandomItems(Random rand) {
        List<String> list = new ArrayList<>();

        int numItems = rand.nextInt(100000);
        for (int i = 0; i < numItems; ++i) {
            StringBuilder b = new StringBuilder();
            b.append("#" + i + ": ");
            int numChars = rand.nextInt(7) + 1;
            for (int j = 0; j < numChars; ++j) {
                char c = (char)('a' + rand.nextInt(26));
                b.append(c);
            }
            String str = b.toString();
            list.add(str);
        }

        return new ListPaginatorFromList<>(list);
    }

    @Test
    public void testEssential() throws IOException {
        boolean isMemory = false;

        Stopwatch sw = Stopwatch.createStarted();

        Random random = new Random(0);

        for (int i = 0; i < 30; ++i) {
            ListPaginator<String> backend = createListWithRandomItems(random);

            testOnce(String.class, backend, backend, 10000, isMemory, "test-" + i, random, random.nextInt(19) + 1, Duration.ofSeconds(10));
        }
        // createListWithRandomItems(random).apply(Range.atLeast(0l)).forEach(System.out::println);

        logger.info("Cache test took: " + sw.elapsed(TimeUnit.MILLISECONDS) * 0.001f + " seconds");
    }


    @Test
    public void testRequestLimits() throws IOException {
        boolean isInMemory = false;

        Stopwatch sw = Stopwatch.createStarted();

        Random random = new Random(0);

        for (int i = 0; i < 30; ++i) {
            int requestLimit = random.nextInt(99) + 1;

            ListPaginator<String> referenceBackend = createListWithRandomItems(random);
            ListPaginator<String> restrictedBackend = new ListPaginator<String>() {
                protected ListPaginator<String> backend = referenceBackend;

                @Override
                public Flowable<String> apply(Range<Long> t) {
                    Range<Long> c = t.canonical(DiscreteDomain.longs());
                    Range<Long> restriction = Range.closedOpen(c.lowerEndpoint(), c.lowerEndpoint() + requestLimit);
                    return backend.apply(restriction);
                }

                @Override
                public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
                    return backend.fetchCount(itemLimit, rowLimit);
                }
            };

            testOnce(String.class, referenceBackend, restrictedBackend, requestLimit, isInMemory,
                    "test-pagination-" + i, random, random.nextInt(19) + 1, Duration.ofSeconds(10));
        }
        // createListWithRandomItems(random).apply(Range.atLeast(0l)).forEach(System.out::println);

        logger.info("Cache test took: " + sw.elapsed(TimeUnit.MILLISECONDS) * 0.001f + " seconds");
    }

    // Large amount of data should spill to disk and not cause out of memory
    @Test
    public void testMemoryUsage() throws IOException {
        boolean isInMemory = false;

        Stopwatch sw = Stopwatch.createStarted();

        int expectedSize = 1000000000;
        ListPaginator<String> backend = ListPaginatorFromList.wrap(createTestList(expectedSize));
        ListPaginator<String> frontend = createCachedListPaginator(String.class, backend, 10000, isInMemory, "test-large",
                Duration.ofSeconds(1));

        long actualSize = frontend.apply(Range.atLeast(0l)).count().blockingGet();
        Assert.assertEquals(expectedSize, actualSize);
        logger.info("Cache test took: " + sw.elapsed(TimeUnit.MILLISECONDS) * 0.001f + " seconds");    }


    public <T> void testOnce(
            Class<T> clazz,
            ListPaginator<T> referenceBackend,
            ListPaginator<T> cachableBackend,
            long requestLimit,
            boolean inMemory,
            String testId,
            Random random,
            int numIterations,
            Duration syncDelay
            ) throws IOException {

        ListPaginator<T> frontend = createCachedListPaginator(clazz, cachableBackend, requestLimit, inMemory, testId,
                syncDelay);

        for (int i = 0; i < numIterations; ++i) {
            int size = Ints.saturatedCast(cachableBackend.fetchCount(null, null).blockingGet().lowerEndpoint());

            int start = random.nextInt(size);
            int end = start + random.nextInt(size - start);

            Range<Long> requestRange = Range.closedOpen((long)start, (long)end);

            logger.info(String.format("Test %s - Iteration %d - Request range %s", testId, i, requestRange));
            List<T> expected = referenceBackend.fetchList(requestRange);
            // List<String> actual = backend.fetchList(requestRange);
            List<T> actual = frontend.fetchList(requestRange);

            Assert.assertEquals(expected, actual);

            // slice.sync();
        }
    }

    public static <T> ListPaginator<T> createCachedListPaginator(Class<T> clazz, ListPaginator<T> cachableBackend,
            long requestLimit, boolean inMemory, String testId, Duration syncDelay) {
        KryoPool kryoPool = KryoUtils.createKryoPool(null);
        ObjectStore objectStore = ObjectStoreImpl.create(Path.of("/tmp/aksw-commons-cache-test"), ObjectSerializerKryo.create(kryoPool));

        org.aksw.commons.path.core.Path<String> objectStoreBasePath = PathOpsStr.newRelativePath("object-store").resolve(testId);

        int pageSize = 1024 * 50;
//        int pageSize = 100;

        ArrayOps<T[]> arrayOps = ArrayOps.createFor(clazz);
        Slice<T[]> slice = inMemory
                ? SliceInMemory.create(arrayOps, new PagedBuffer<>(arrayOps, pageSize))
                : SliceWithPagesSyncToDisk.create(ArrayOps.createFor(clazz), objectStore, objectStoreBasePath, pageSize, syncDelay);

        Builder<T[]> builder = AdvancedRangeCacheImpl.Builder.<T[]>create()
            // .setDataSource(SequentialReaderSourceRx.create(ArrayOps.createFor(String.class), backend))
            .setRequestLimit(requestLimit)
            .setWorkerBulkSize(128)
            .setSlice(slice)
            .setTerminationDelay(Duration.ofSeconds(5));

        //  SmartRangeCacheNew<String> cache
        ListPaginator<T> frontend = ListPaginatorWithAdvancedCache.create(cachableBackend, builder);
        return frontend;
    }
}
