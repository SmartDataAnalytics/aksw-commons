package org.aksw.commons.rx.function;

import java.io.Serializable;

import org.aksw.commons.lambda.serializable.SerializableFunction;
import org.reactivestreams.Publisher;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;

/** A FlowableTransformer with support for method chaining */
@FunctionalInterface
public interface RxFunction<I, O>
    extends FlowableTransformer<I, O>, Serializable
{
    /**
     * Generic chaining:
     * RxFunction<I, O> fn = RxFunction.<I>identity()
     *   .andThen(a -> fn(a))...andThen(o -> fn(o));...
     *
     * @param <X>
     * @param next
     * @return
     */
    default <X> RxFunction<I, X> andThen(RxFunction<? super O, X> next) {
        return in -> {
            Publisher<O> o = this.apply(in);
            Publisher<X> r = next.apply(Flowable.fromPublisher(o));
            return r;
        };
    }

    /**
     * {@code andThenMap(x -> y) is a short hand for {@code andThen(flowable -> flowable.map(x -> y))}
     *
     * @param <X>
     * @param mapper
     * @return
     */
    default <X> RxFunction<I, X> andThenMap(SerializableFunction<? super O, X> mapper) {
        return in -> {
            Publisher<O> o = this.apply(in);
            Publisher<X> r = Flowable.fromPublisher(o).map(mapper::apply);
            return r;
        };
    }

    default <X> RxFunction<I, X> andThenFlatMapIterable(SerializableFunction<? super O, ? extends Iterable<X>> mapper) {
        return in -> {
            Publisher<O> o = this.apply(in);
            Publisher<X> r = Flowable.fromPublisher(o).flatMapIterable(mapper::apply);
            return r;
        };
    }

    static <I, O> RxFunction<I, O> from(RxFunction<I, O> rxfn) {
        return rxfn;
    }


    static <X> RxFunction<X, X> identity() {
        return x -> x;
    }
}
