package org.aksw.commons.rx.op;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.Test;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FlowableOperatorReadAheadTests {

    public static <T> FlowableTransformer<T, T> delayEach(long time, TimeUnit timeUnit) {
        return upstream -> upstream.concatMap(item -> Flowable.interval(time, timeUnit)
                .take(1)
                .map(second -> item));
    }

    @Test
    public void test() {
        Flowable<Integer> core = Flowable.fromIterable(() ->
            IntStream.range(0, 20).boxed().peek(x -> System.out.println("Read: " + x)).iterator());

        // core.forEach(item -> System.out.println("Got: " + item));

        core
            .lift(FlowableOperatorReadAhead.create(3))
            .compose(delayEach(1, TimeUnit.SECONDS))
            .subscribeOn(Schedulers.io())
            .blockingForEach(item -> {
                System.out.println("Consumed " + item);
            });
    }
}
