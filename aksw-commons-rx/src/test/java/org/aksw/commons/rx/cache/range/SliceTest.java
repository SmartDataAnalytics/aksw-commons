package org.aksw.commons.rx.cache.range;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.aksw.commons.store.object.key.api.KeyObjectStore;
import org.aksw.commons.store.object.key.impl.KeyObjectStoreFromMap;
import org.junit.Assert;
import org.junit.Test;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SliceTest {
    @Test
    public void test1() throws ExecutionException, InterruptedException {

//        return SmartRangeCacheImpl.wrap(
//                backend, SmartRangeCacheImpl.createKeyObjectStore(Paths.get("/tmp/test"), SmartRangeCacheImpl.createKyroPool(null)), 1024, 10, Duration.ofSeconds(1), 10000, 1000);

//        KryoPool kryoPool = SmartRangeCacheImpl.createKyroPool(null);
//        KeyObjectStore keyObjectStore = SmartRangeCacheImpl.createKeyObjectStore(Paths.get("/tmp/test"), kryoPool);


        KeyObjectStore keyObjectStore = new KeyObjectStoreFromMap();

        Slice<String> buffer = new SliceWithPagesImpl<>(
                keyObjectStore, 1, 10, Duration.ofSeconds(1));

        int totalSize = 7;

        int start = 5;
        int length = 5;

        int effLength = Math.min(length, totalSize - start);

        Future<List<String>> future = Flowable.fromIterable(() -> buffer.blockingIterator(start))
                .subscribeOn(Schedulers.computation())
                .take(length)
                .toList()
                .toFuture();

        String[] arr = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
        buffer.putAll(0, arr, 0, totalSize);

        buffer.mutateMetaData(metaData -> metaData.setMaximumKnownSize(totalSize));

        List<String> expected = Arrays.asList(arr).subList(start, start + effLength);
        List<String> actual = future.get();
        Assert.assertEquals(expected, actual);
    }

}
