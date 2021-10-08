package org.aksw.commons.rx.util;

import java.util.Iterator;

import org.aksw.commons.lambda.throwing.ThrowingBiConsumer;
import org.aksw.commons.lambda.throwing.ThrowingConsumer;
import org.aksw.commons.lambda.throwing.ThrowingFunction;
import org.aksw.commons.lambda.throwing.ThrowingPredicate;
import org.aksw.commons.lambda.throwing.ThrowingSupplier;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Additional factory methods for creating flowables, such as based on IO
 *
 * @author raven
 *
 */
public class FlowableEx {

    /** REMOVED: Create a flowable from a supplier of iterators */
    /** REASON: Just use Flowable.fromIterable(() -> it); */
//    public static <T, I extends Iterator<T>> Flowable<T> fromIteratorSupplier(
//            ThrowingSupplier<I> itSupp) {
//        return FlowableEx.fromIteratorSupplier(itSupp, null);
//    }

    /** Create a flowable from a supplier of (closeable) iterator */
    public static <T, I extends Iterator<T>> Flowable<T> fromIteratorSupplier(
            ThrowingSupplier<I> itSupp,
            ThrowingConsumer<? super I> closer) {

        ThrowingConsumer<? super I> closeAction = closer == null ? it -> {} : closer;

        return Flowable.<T, I>generate(
            itSupp::get,
            (it, e) -> {
                try {
                    if (it.hasNext()) {
                        T item = it.next();
                        e.onNext(item);
                    } else {
                        e.onComplete();
                    }
                } catch (Exception x) {
                    e.onError(x);
                }
            },
            closeAction::accept);
    }


    /**
     * Create a flowable from a supplier of resources with a subsequent processing into an iterator.
     *
     * It is important to understand that resource acquisition happens during state
     * initialization, whereas iterable initialization is done in the generator.
     * This gives control over when exception handling: During state initialization there is
     * no emitter and therefore exceptions cannot be passed downstream. Conversely, exceptions ocurring
     * in the generator are always forwarded.
     *
     * Example use case: supply an input stream, create a commons CSV parser from it and
     * then yield items from its iterator.
     *
     * @param <T> The item type
     * @param <R> The resource type
     * @param <C> The (conceptual) iterable derived from the resource (does not have to implement {@link Iterable})
     * @param <I> The iterator derived from the iterable
     *
     * @param resourceSupplier
     * @param toIterable
     * @param toIterator
     * @param closer
     * @return
     */
    public static <T, R, C> Flowable<T> fromIterableResource(
            ThrowingSupplier<R> resourceSupplier,
            ThrowingFunction<? super R, C> toIterable,
            ThrowingFunction<? super C, ? extends Iterator<T>> toIterator,
            ThrowingBiConsumer<? super R, ? super C> closer) {

        Flowable<T> result = Flowable.<T, IterableResourceState<R, C, T>>generate(
                () -> new IterableResourceState<>(resourceSupplier.get()),
                (state, emitter) -> {
                    try {
                        Iterator<T> iterator;
                        if ((iterator = state.iterator) == null) {
                            C iterable = toIterable.apply(state.resource);
                            iterator = state.iterator = toIterator.apply(iterable);
                        }

                        if (iterator.hasNext()) {
                            T item = iterator.next();
                            emitter.onNext(item);
                        } else {
                            emitter.onComplete();
                        }
                    } catch(Exception e) {
                        emitter.onError(e);
                    }
                },
                state -> closer.accept(state.resource, state.iterable));
        return result;
    }

    /**
     * Similar to {@link #fromIterableResource(ThrowingSupplier, ThrowingFunction, ThrowingFunction, ThrowingBiConsumer)}.
     * Instead of an iterator there is just a 'nextRecord' method. Typicall a result of null
     * indicates the end of data.
     *
     * @param <T>
     * @param <R>
     * @param <E>
     * @param resourceSupplier
     * @param toEnumerable
     * @param nextRecord
     * @param closer
     * @return
     */
    public static <T, R, E> Flowable<T> fromEnumerableResource(
            ThrowingSupplier<R> resourceSupplier,
            ThrowingFunction<? super R, E> toEnumerable,
            ThrowingFunction<? super E, T> nextRecord,
            ThrowingBiConsumer<? super R, ? super E> closer) {

        Flowable<T> result = Flowable.<T, IterableResourceState<R, E, T>>generate(
                () -> new IterableResourceState<>(resourceSupplier.get()),
                (state, emitter) -> {
                    try {
                        E enumerable = state.iterable;
                        if (enumerable == null) {
                            enumerable = state.iterable = toEnumerable.apply(state.resource);
                        }

                        T record;
                        if ((record = nextRecord.apply(enumerable)) != null) {
                            emitter.onNext(record);
                        } else {
                            emitter.onComplete();
                        }
                    } catch(Exception e) {
                        emitter.onError(e);
                    }
                },
                state -> closer.accept(state.resource, state.iterable));
        return result;
    }


    /**
     * Helper class used with {@link FlowableEx#fromIterableResource(ThrowingSupplier, ThrowingFunction, ThrowingFunction, ThrowingBiConsumer)}
     */
    private static class IterableResourceState<R, C, T> {
        R resource;
        C iterable;
        Iterator<T> iterator;

        public IterableResourceState(R resource) {
            this.resource = resource;
        }
    }





}
