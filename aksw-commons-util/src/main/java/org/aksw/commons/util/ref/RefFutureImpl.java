package org.aksw.commons.util.ref;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

public class RefFutureImpl<T>
    extends RefDelegateBase<CompletableFuture<T>, Ref<CompletableFuture<T>>>
    implements RefFuture<T>
{
    public RefFutureImpl(Ref<CompletableFuture<T>> delegate) {
        super(delegate);
    }

    public static <T> RefFuture<T> wrap(Ref<CompletableFuture<T>> delegate) {
        return new RefFutureImpl<T>(delegate);
    }

    @Override
    public RefFuture<T> acquire() {
        return wrap(getDelegate().acquire());
    }


    public static <T> RefFuture<T> fromFuture(CompletableFuture<Ref<T>> future, Object synchronizer) {
      return wrap(RefImpl.create(future.thenApply(Ref::get), synchronizer, () -> {
          try {
              boolean isCancelled = future.cancel(true);
              System.out.println("isCancelled: " + isCancelled);
              if (!isCancelled) {
                  Ref<T> ref = future.get();
                  if (ref != null) {
                      ref.close();
                  }
              }
          } catch (CancellationException e) {
              System.out.println("CancelException");
          }
      }, null));
  }

}