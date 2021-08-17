package org.aksw.commons.rx.cache.range;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.aksw.commons.util.range.RangeBuffer;
import org.aksw.commons.util.range.RangeBufferStandaloneImpl;
import org.junit.Test;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import junit.framework.Assert;

public class RangeBufferTest {
//    @Test
//    public void test1() throws ExecutionException, InterruptedException {
//
//        RangeBuffer<String> buffer = new RangeBufferStandaloneImpl<>(10);
//
//        Future<List<String>> future = Flowable.fromIterable(() -> buffer.blockingIterator(5))
//                .subscribeOn(Schedulers.computation())
//                .take(5)
//                .toList()
//                .toFuture();
//
//        String[] arr = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
//        buffer.putAll(0, arr);
//
//        List<String> expected = Arrays.asList(arr).subList(5, 10);
//        List<String> actual = future.get();
//        Assert.assertEquals(expected, actual);
//    }
}
