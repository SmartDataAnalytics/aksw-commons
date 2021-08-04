package org.aksw.commons.rx.cache.range;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.aksw.commons.rx.op.LocalOrderSpec;
import org.aksw.commons.rx.op.LocalOrderSpecImpl;
import org.aksw.commons.rx.op.OperatorLocalOrder;
import org.aksw.commons.store.object.key.api.KeyObjectStore;
import org.aksw.commons.util.range.RangeBuffer;
import org.aksw.commons.util.range.RangeBufferImpl;
import org.aksw.commons.util.ref.RefFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;

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
        main1();
    }

    private static final Logger logger = LoggerFactory.getLogger(LocalOrderAsyncTest.class);

    public static <V> AsyncClaimingCache<Long, RangeBuffer<V>> syncedRangeBuffer(
            long maximumSize,
            Duration syncDelayDuration,
            KeyObjectStore store,
            Supplier<RangeBuffer<V>> newValue) {

        Map<Long, Long> keyToVersion = Collections.synchronizedMap(new IdentityHashMap<>());

        AsyncClaimingCache<Long, RangeBuffer<V>> result = AsyncClaimingCache.create(
                syncDelayDuration,

                // begin of level3 setup
                   Caffeine.newBuilder()
                   .scheduler(Scheduler.systemScheduler())
                   .maximumSize(maximumSize), //.expireAfterWrite(1, TimeUnit.SECONDS),
                   key -> {
                       List<String> internalKey = Arrays.asList(Long.toString(key));
                       RangeBuffer<V> value;
                       try {
                           value = store.computeIfAbsent(internalKey, newValue::get);
                       } catch (Exception e) {
                           // logger.warn("Error", e);
                           throw new RuntimeException(e);
                          //  value = newValue.get(); //new RangeBufferImpl<V>(1024);
                       }

                       RangeBuffer<V> r = value;
                       long generation = value.getGeneration();

                       keyToVersion.put(key, generation);

    //                   Ref<V> r = RefImpl.create(v, null, () -> {
    //                       // Sync the page upon closing it
    //                       store.put(internalKey, v);
    //                       logger.info("Synced " + internalKey);
    //                       System.out.println("Synced" + internalKey);
    //                   });
    //
                       return r;

                   },
                   (key, value, cause) -> {
                       keyToVersion.remove(key);
                   },
                // end of level3 setup

                (key, value, cause) -> {
                    List<String> internalKey = Arrays.asList(Long.toString(key));

                    Lock readLock = value.getReadWriteLock().readLock();
                    readLock.lock();

                    long version = keyToVersion.get(key);

                    long generation = value.getGeneration();

                    try {
                        if (generation != version) {
                            logger.info("Syncing dirty buffer " + internalKey);
                            keyToVersion.put(key, generation);
                            store.put(internalKey, value);
                        } else {
                            logger.info("Syncing not needed because of clean buffer " + internalKey);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        readLock.unlock();
                    }
                }
            );

        return result;
    }



    public static void main1() throws Exception {
        KeyObjectStore objStore = SmartRangeCacheImpl.createKeyObjectStore(Paths.get("/tmp/test/"), null);

        List<String> key = Arrays.asList("q1", "100");
        RangeBuffer<String> value = new RangeBufferImpl<>(1024);
        value.put(0, "hello");
        objStore.put(key, value);



        RangeBuffer<String> restored = objStore.get(key);
        System.out.println(restored.blockingIterator(0));
        System.out.println(restored.getKnownSize());



        AsyncClaimingCache<Long, RangeBuffer<String>> cache = syncedRangeBuffer(10, Duration.ofSeconds(1), objStore, () -> new RangeBufferImpl<String>(1024));

        // troll the system: Acquire a page which we want to load in a moment
        // and cancel its request
        // This may potentially sometimes cause troubles with kryo - its not clear whether this is harmless:
        // [kryo] Unable to load class  with kryo's ClassLoader. Retrying with current..
        cache.claim(1024l).close();

        try (RefFuture<RangeBuffer<String>> page1 = cache.claim(1024l)) {
            page1.await().put(10, "hello!!!");
        }

        try (RefFuture<RangeBuffer<String>> page2 = cache.claim(2048l)) {
            page2.await().put(15, "world");
        }

        try (RefFuture<RangeBuffer<String>> page1 = cache.claim(1024l)) {
            System.out.println(page1.await().blockingIterator(10).next());
        }

        try (RefFuture<RangeBuffer<String>> page2 = cache.claim(2048l)) {
            System.out.println(page2.await().blockingIterator(15).next());
        }



        cache.invalidateAll();


    }

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
