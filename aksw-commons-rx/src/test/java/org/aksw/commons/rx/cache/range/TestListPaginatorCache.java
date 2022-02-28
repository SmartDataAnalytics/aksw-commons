package org.aksw.commons.rx.cache.range;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.aksw.commons.path.core.PathOpsStr;
import org.aksw.commons.rx.cache.range.AdvancedRangeCacheNew.Builder;
import org.aksw.commons.rx.lookup.ListPaginator;
import org.aksw.commons.rx.lookup.ListPaginatorFromList;
import org.aksw.commons.store.object.key.api.ObjectStore;
import org.aksw.commons.store.object.key.impl.ObjectStoreImpl;
import org.aksw.commons.store.object.path.impl.ObjectSerializerKryo;
import org.aksw.commons.util.array.ArrayOps;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;
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
        Stopwatch sw = Stopwatch.createStarted();

        Random random = new Random(0);

        for (int i = 0; i < 10; ++i) {
            testOnce("test" + i, random, random.nextInt(9) + 1);
        }
        // createListWithRandomItems(random).apply(Range.atLeast(0l)).forEach(System.out::println);

        logger.info("Cache test took: " + sw.elapsed(TimeUnit.MILLISECONDS) * 0.001f + " seconds");


    }

    public void testOnce(String testId, Random random, int numIterations) throws IOException {
        ListPaginator<String> backend = createListWithRandomItems(random);

        KryoPool kryoPool = SmartRangeCacheImpl.createKyroPool(null);
        ObjectStore objectStore = ObjectStoreImpl.create(Path.of("/tmp/aksw-commons-cache-test"), ObjectSerializerKryo.create(kryoPool));

        org.aksw.commons.path.core.Path<String> objectStoreBasePath = PathOpsStr.newRelativePath("object-store").resolve(testId);

         int pageSize = 1024 * 50;
//        int pageSize = 100;
        SliceBufferNew<String[]> slice = SliceBufferNew.create(ArrayOps.createFor(String.class), objectStore, objectStoreBasePath, pageSize, Duration.ofMillis(500));

        Builder<String[]> builder = AdvancedRangeCacheNew.Builder.<String[]>create()
            // .setDataSource(SequentialReaderSourceRx.create(ArrayOps.createFor(String.class), backend))
            .setRequestLimit(10000)
            .setWorkerBulkSize(128)
            .setSlice(slice)
            .setTerminationDelay(Duration.ofSeconds(10));

        //  SmartRangeCacheNew<String> cache
        ListPaginator<String> frontend = SmartRangeCacheNew.create(backend, builder);


        for (int i = 0; i < numIterations; ++i) {
            int size = Ints.saturatedCast(backend.fetchCount(null, null).blockingGet().lowerEndpoint());

            int start = random.nextInt(size);
            int end = start + random.nextInt(size - start);

            Range<Long> requestRange = Range.closedOpen((long)start, (long)end);

            List<String> expected = backend.fetchList(requestRange);
            // List<String> actual = backend.fetchList(requestRange);
            List<String> actual = frontend.fetchList(requestRange);

            Assert.assertEquals(expected, actual);


            slice.sync();
        }
    }
}
