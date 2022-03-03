package org.aksw.commons.rx.util;

import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import io.reactivex.rxjava3.core.Flowable;

public class FlowableUtils {
    /**
     * Generic helper to create a Flowable by mapping some resource such as in InputStream or
     * a QueryExecution to an iterable such as an ResultSet
     *
     * @param <R>
     * @param <I>
     * @param <T>
     * @param resourceSupplier
     * @param resourceToIterator
     * @param hasNext
     * @param next
     * @param closeResource
     * @return
     */
    public static <R, I, T> Flowable<T> createFlowableFromResource(
            Callable<R> resourceSupplier,
            Function<? super R, I> resourceToIterator,
            Predicate<? super I> hasNext,
            Function<? super I, T> next,
            Consumer<? super R> closeResource) {

        Flowable<T> result = Flowable.generate(
                () -> {
                    R in = resourceSupplier.call();
                    return new SimpleEntry<R, I>(in, null);
                },
                (state, emitter) -> {
                    I it = state.getValue();

                    try {
                        if (it == null) {
                            R in = state.getKey();
                            it = resourceToIterator.apply(in);
                            state.setValue(it);
                        }

                        boolean hasMore = hasNext.test(it);
                        if (hasMore) {
                            T value = next.apply(it);
                            emitter.onNext(value);
                        } else {
                            emitter.onComplete();
                        }
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                },
                state -> {
                    R in = state.getKey();
                    if (in != null) {
                        closeResource.accept(in);
                    }
                });

        return result;
    }
}
