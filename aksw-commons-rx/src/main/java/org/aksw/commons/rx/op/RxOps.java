package org.aksw.commons.rx.op;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.LongStream;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.parallel.ParallelTransformer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RxOps {

    /**
     * Factory method for yielding a FlowableTransformer that applies a given parallelTransformer
     * thereby providing wrapping for local ordering so that items are emitted in order
     *
     * @param <I>
     * @param <O>
     * @param flatMapper
     * @return
     */
    public static <I, O> FlowableTransformer<I, O> createParallelMapperOrderedCore(
            ParallelTransformer<Entry<I, Long>, Entry<O, Long>> parallelTransformer) {
        return in ->  in
                .zipWith(() -> LongStream.iterate(0, i -> i + 1).iterator(), (k, v) -> (Entry<I, Long>)new SimpleEntry<>(k, v))
                .parallel()
                .runOn(Schedulers.io())
                .compose(parallelTransformer)
                .sequential()
                .lift(OperatorLocalOrder.forLong(0l, Entry::getValue))
                .map(Entry::getKey);
    }

    /**
     * Factory method for yielding a FlowableTransformer that applies a given flatMap function in parallel
     * but apply local ordering so that items are emitted in order
     *
     * @param <I>
     * @param <O>
     * @param flatMapper
     * @return
     */
    public static <I, O> FlowableTransformer<I, O> createParallelMapperOrdered(
            Function<? super I, O> mapper) {
        return createParallelMapperOrderedCore(
                in -> in.map(e -> {
                    I before = e.getKey();
                    O after = mapper.apply(before);
                    Entry<O, Long> r = new SimpleEntry<>(after, e.getValue());
                    return r;
                }));
    }

    public static <I, O> FlowableTransformer<I, O> createParallelFlatMapperOrdered(
            Function<? super I, ? extends Iterable<? extends O>> mapper) {
        return in -> in.compose(createParallelMapperOrdered(mapper))
                .concatMap(Flowable::fromIterable);
    }

//    public static <I, O> FlowableTransformer<I, O> createParallelFlatMapperOrderedStream(
//            Function<? super I, ? extends Stream<O>> mapper) {
//        return createParallelMapperOrderedCore(
//                in -> in.flatMap(e -> {
//                    I before = e.getKey();
//                    Stream<O> after = mapper.apply(before);
//                    Entry<O, Long> r = new SimpleEntry<>(after, e.getValue());
//                    return r;
//                }));
//    }

}
