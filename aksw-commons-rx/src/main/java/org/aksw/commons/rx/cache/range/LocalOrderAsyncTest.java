package org.aksw.commons.rx.cache.range;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.aksw.commons.rx.op.LocalOrderSpec;
import org.aksw.commons.rx.op.LocalOrderSpecImpl;
import org.aksw.commons.rx.op.OperatorLocalOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subjects.PublishSubject;

class MyPublisher<T, S extends Comparable<S>> {
    protected PublishSubject<T> publishSubject = PublishSubject.create();
    protected LocalOrderSpec<T, S> orderSpec;


    public MyPublisher(LocalOrderSpec<T, S> orderSpec) {
        super();
        this.orderSpec = orderSpec;
    }

    public static <T, S extends Comparable<S>> MyPublisher<T, S> create(LocalOrderSpec<T, S> orderSpec) {
        return new MyPublisher<T, S>(orderSpec);
    }

    public void offer(T item) {
        publishSubject.onNext(item);
    }

    public Flowable<T> createFlow(S start) {
        return publishSubject
            .toFlowable(BackpressureStrategy.ERROR)
            .filter(item -> orderSpec.getDistanceFn().apply(start, orderSpec.getExtractSeqId().apply(item)).longValue() > 0)
            .lift(OperatorLocalOrder.<T, S>create(start, orderSpec));
    }
}


public class LocalOrderAsyncTest {
    public static void main(String[] args) throws Exception {
//        main1();
    }

    private static final Logger logger = LoggerFactory.getLogger(LocalOrderAsyncTest.class);

//
//    public static void main1() throws Exception {
//        KeyObjectStore objStore = SmartRangeCacheImpl.createKeyObjectStore(Paths.get("/tmp/test/"), null);
//
//        List<String> key = Arrays.asList("q1", "100");
//        BufferWithGeneration<String> value = new BufferWithGeneration<>(1024);
//        value.put(0, "hello");
//        objStore.put(key, value);
//
//
//
//        RangeBuffer<String> restored = objStore.get(key);
//        System.out.println(restored.blockingIterator(0));
//        System.out.println(restored.getKnownSize());
//
//
//
//        AsyncClaimingCache<Long, RangeBuffer<String>> cache = AsyncClaimingCacheWithTransformValue.create(
//                SmartRangeCacheImpl.syncedBuffer(
//                    10,
//                    Duration.ofSeconds(1),
//                    objStore,
//                    () -> new RangeBufferStandaloneImpl<String>(1024)),
//                Entry::getKey);
//
//        // troll the system: Acquire a page which we want to load in a moment
//        // and cancel its request
//        // This may potentially sometimes cause troubles with kryo - its not clear whether this is harmless:
//        // [kryo] Unable to load class  with kryo's ClassLoader. Retrying with current..
//        cache.claim(1024l).close();
//
//        try (RefFuture<RangeBuffer<String>> page1 = cache.claim(1024l)) {
//            page1.await().put(10, "hello!!!");
//        }
//
//        try (RefFuture<RangeBuffer<String>> page2 = cache.claim(2048l)) {
//            page2.await().put(15, "world");
//        }
//
//        try (RefFuture<RangeBuffer<String>> page1 = cache.claim(1024l)) {
//            System.out.println(page1.await().blockingIterator(10).next());
//        }
//
//        try (RefFuture<RangeBuffer<String>> page2 = cache.claim(2048l)) {
//            System.out.println(page2.await().blockingIterator(15).next());
//        }
//
//
//
//        cache.invalidateAll();
//
//
//    }

    public static void main2() {
        LocalOrderSpec<Long, Long> spec = LocalOrderSpecImpl.forLong(x -> x);

        MyPublisher<Long, Long> publisher = MyPublisher.create(spec);

        publisher.createFlow(90l).forEach(x -> System.out.println("GOT A: " + x));
        publisher.createFlow(95l).forEach(x -> System.out.println("GOT B: " + x));


        List<Long> longs = LongStream.range(0l, 100l).boxed().collect(Collectors.toList());
        Collections.shuffle(longs, new Random(0));


        longs.forEach(publisher::offer);


    }
}
