package org.aksw.commons.rx.cache.range;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.aksw.commons.rx.op.LocalOrderSpec;
import org.aksw.commons.rx.op.LocalOrderSpecImpl;
import org.aksw.commons.rx.op.OperatorLocalOrder;

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
            .lift(OperatorLocalOrder.<T, S>create(start, orderSpec));
    }
}

public class AsyncLoadTest {
    public static void main(String[] args) {
        LocalOrderSpec<Long, Long> spec = LocalOrderSpecImpl.forLong(x -> x);

        MyPublisher<Long, Long> publisher = MyPublisher.create(spec);

        publisher.createFlow(90l).forEach(x -> System.out.println("GOT A: " + x));
        publisher.createFlow(95l).forEach(x -> System.out.println("GOT B: " + x));


        List<Long> longs = LongStream.range(0l, 100l).boxed().collect(Collectors.toList());
        Collections.shuffle(longs, new Random(0));


        longs.forEach(publisher::offer);


    }
}
