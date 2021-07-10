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
}