package org.aksw.commons.util.ref;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface RefFuture<T>
    extends RefDelegate<CompletableFuture<T>, Ref<CompletableFuture<T>>>
{
    default T await() throws InterruptedException, ExecutionException {
        return get().get();
    }

    @Override
    RefFuture<T> acquire();



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