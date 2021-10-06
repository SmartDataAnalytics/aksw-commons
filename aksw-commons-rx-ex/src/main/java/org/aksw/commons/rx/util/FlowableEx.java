package org.aksw.commons.rx.util;

import java.util.Iterator;

import org.aksw.commons.lambda.throwing.ThrowingBiConsumer;
import org.aksw.commons.lambda.throwing.ThrowingConsumer;
import org.aksw.commons.lambda.throwing.ThrowingFunction;
import org.aksw.commons.lambda.throwing.ThrowingSupplier;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Additional factory methods for creating flowables, such as based on IO
 *
 * @author raven
 *
 */
public class FlowableEx {

    /** Create a flowable from a supplier of iterators */
    public static <T, I extends Iterator<T>> Flowable<T> fromIteratorSupplier(
            ThrowingSupplier<I> itSupp) {
        return FlowableEx.fromIteratorSupplier(itSupp, null);
    }

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



    private static class State<R, C, I> {
        R resource;
        C iterable;
        I iterator;

        public State(R resource) {
            this.resource = resource;
        }
    }


    /**
     * Create a flowable from a supplier of resources with a subsequent processing.
     *
     * For example, supply an input stream, create a commons CSV parser from it and
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
    public static <T, R, C, I extends Iterator<T>> Flowable<T> fromIterableResource(
            ThrowingSupplier<R> resourceSupplier,
            ThrowingFunction<R, C> toIterable,
            ThrowingFunction<C, I> toIterator,
            ThrowingBiConsumer<R, C> closer) {

        Flowable<T> result = Flowable.<T, State<R, C, I>>generate(
                () -> {
                    return new State<>(resourceSupplier.get());
                },
                (state, emitter) -> {
                    try {
                        I iterator;
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

}
