package org.aksw.commons.rx.cache.range;

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
