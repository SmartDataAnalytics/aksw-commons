package org.aksw.commons.util.stream;
import java.io.Serializable;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.aksw.commons.lambda.serializable.SerializableFunction;

/**
 * Helper interface to make transformations on streams less verbose
 *
 * Inherits from {@link Function} for out of the box chaining with {@link #andThen(Function)}.
 *
 * @author Claus Stadler 11/09/2020
 *
 * @param <I>
 * @param <O>
 */
@FunctionalInterface
public interface StreamFunction<I, O>
    extends Function<Stream<I>, Stream<O>>, Serializable
{
    /**
     * Generic chaining:
     * StreamFunction<I, O> fn = StreamFunction.<I>identity()
     *   .andThen(a -> fn(a))...andThen(o -> fn(o));...
     *
     * @param <X>
     * @param next
     * @return
     */
    default <X> StreamFunction<I, X> andThen(StreamFunction<O, X> next) {
        return in -> {
            Stream<O> o = this.apply(in);
            Stream<X> r = next.apply(o);
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
    default <X> StreamFunction<I, X> andThenMap(SerializableFunction<O, X> mapper) {
        return in -> {
            Stream<O> o = this.apply(in);
            Stream<X> r = o.map(mapper::apply);
            return r;
        };
    }

    default <X> StreamFunction<I, X> andThenFlatMap(SerializableFunction<O, ? extends Stream<X>> mapper) {
        return in -> {
            Stream<O> o = this.apply(in);
            Stream<X> r = o.flatMap(x -> mapper.apply(x));
            return r;
        };
    }

    default <X> StreamFunction<I, X> andThenFlatMapIterable(SerializableFunction<O, ? extends Iterable<X>> mapper) {
        return in -> {
            Stream<O> o = this.apply(in);
            Stream<X> r = o.flatMap(x -> StreamSupport.stream(mapper.apply(x).spliterator(), false));
            return r;
        };
    }

    static <I, O> StreamFunction<I, O> from(StreamFunction<I, O> rxfn) {
        return rxfn;
    }


    static <X> StreamFunction<X, X> identity() {
        return x -> x;
    }
}
