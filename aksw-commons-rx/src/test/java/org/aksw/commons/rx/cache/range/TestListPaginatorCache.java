package org.aksw.commons.rx.cache.range;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.io.buffer.plain.PagedBuffer;
import org.aksw.commons.io.cache.AdvancedRangeCacheImpl;
import org.aksw.commons.io.cache.AdvancedRangeCacheImpl.Builder;
import org.aksw.commons.io.slice.SliceBufferNew;
import org.aksw.commons.io.slice.SliceInMemory;
import org.aksw.commons.io.slice.SliceWithAutoSync;
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
import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import com.google.common.primitives.Ints;

public class TestListPaginatorCache {

    private static final Logger logger = LoggerFactory.getLogger(TestListPaginatorCache.class);

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
    public void test() throws IOException {
        boolean isMemory = false;

        Stopwatch sw = Stopwatch.createStarted();

        Random random = new Random(0);

        for (int i = 0; i < 30; ++i) {
            ListPaginator<String> backend = createListWithRandomItems(random);

            testOnce(String.class, backend, isMemory, "test" + i, random, random.nextInt(19) + 1);
        }
        // createListWithRandomItems(random).apply(Range.atLeast(0l)).forEach(System.out::println);

        logger.info("Cache test took: " + sw.elapsed(TimeUnit.MILLISECONDS) * 0.001f + " seconds");
    }

    public <T> void testOnce(
            Class<T> clazz, ListPaginator<T> backend, boolean inMemory, String testId, Random random, int numIterations) throws IOException {

        KryoPool kryoPool = KryoUtils.createKyroPool(null);
        ObjectStore objectStore = ObjectStoreImpl.create(Path.of("/tmp/aksw-commons-cache-test"), ObjectSerializerKryo.create(kryoPool));

        org.aksw.commons.path.core.Path<String> objectStoreBasePath = PathOpsStr.newRelativePath("object-store").resolve(testId);

        int pageSize = 1024 * 50;
//        int pageSize = 100;

        ArrayOps<T[]> arrayOps = ArrayOps.createFor(clazz);
        SliceWithAutoSync<T[]> slice = inMemory
                ? SliceInMemory.create(arrayOps, new PagedBuffer<>(arrayOps, pageSize))
                : SliceBufferNew.create(ArrayOps.createFor(clazz), objectStore, objectStoreBasePath, pageSize, Duration.ofMillis(500));

        Builder<T[]> builder = AdvancedRangeCacheImpl.Builder.<T[]>create()
            // .setDataSource(SequentialReaderSourceRx.create(ArrayOps.createFor(String.class), backend))
            .setRequestLimit(10000)
            .setWorkerBulkSize(128)
            .setSlice(slice)
            .setTerminationDelay(Duration.ofSeconds(10));

        //  SmartRangeCacheNew<String> cache
        ListPaginator<T> frontend = ListPaginatorWithAdvancedCache.create(backend, builder);


        for (int i = 0; i < numIterations; ++i) {
            int size = Ints.saturatedCast(backend.fetchCount(null, null).blockingGet().lowerEndpoint());

            int start = random.nextInt(size);
            int end = start + random.nextInt(size - start);

            Range<Long> requestRange = Range.closedOpen((long)start, (long)end);

            List<T> expected = backend.fetchList(requestRange);
            // List<String> actual = backend.fetchList(requestRange);
            List<T> actual = frontend.fetchList(requestRange);

            Assert.assertEquals(expected, actual);

            // slice.sync();
        }
    }
}
