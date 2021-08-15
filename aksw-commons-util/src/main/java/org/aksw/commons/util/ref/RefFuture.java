package org.aksw.commons.util.ref;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public interface RefFuture<T>
    extends RefDelegate<CompletableFuture<T>, Ref<CompletableFuture<T>>>
{
    default T await() throws InterruptedException, ExecutionException {
        return get().get();
    }

    @Override
    RefFuture<T> acquire();


    /** Create a sub-reference to a transformed value of the CompletableFuture */
    default <U> RefFuture<U> acquireTransformed(Function<? super T, ? extends U> transform) {
        RefFuture<T> acquired = this.acquire();
        CompletableFuture<U> future = acquired.get().thenApply(transform);
        RefFuture<U> result = new RefFutureImpl<>(RefImpl.create(future, getSynchronizer(), acquired::close));
        return result;
    }

    default <U> RefFuture<U> acquireTransformedAndCloseThis(Function<? super T, ? extends U> transform) {
        RefFuture<U> result = acquireTransformed(transform);
        this.close();
        return result;
    }

/*
// Not sure whether viewing RefFuture<T> as CompletableFuture<Ref<T>> is really needed.
    default CompletableFuture<Ref<T>> toFuture() {
        CompletableFuture<Ref<T>> result = wrap(this);
        return result;
    }


    static <T> CompletableFuture<Ref<T>> wrap(RefFuture<T> ref) {
        // Return a new future that closes the ref on cancel
        CompletableFuture<Ref<T>> result =
                new CompletableFuture<Ref<T>>()
                .whenCompleteAsync((v, t) -> {
                    if (t != null) {
                        ref.close();
                    }
                });

        ref.get()
            .thenApply(v -> RefImpl.create(v, ref.getSynchronizer(), ref::close))
            .whenCompleteAsync((v, t) -> {
                if (t != null) {
                    result.completeExceptionally(t);
                } else {
                    result.complete(v);
                }
            });

        return result;
    }
*/

}