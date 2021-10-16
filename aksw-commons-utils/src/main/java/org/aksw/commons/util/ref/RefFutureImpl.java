package org.aksw.commons.util.ref;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefFutureImpl<T>
    extends RefDelegateBase<CompletableFuture<T>, Ref<CompletableFuture<T>>>
    implements RefFuture<T>
{
    private static final Logger logger = LoggerFactory.getLogger(RefFutureImpl.class);

    public RefFutureImpl(Ref<CompletableFuture<T>> delegate) {
        super(delegate);
    }

    @Override
    public RefFuture<T> acquire() {
        return wrap(getDelegate().acquire());
    }

    /**
     * A simple wrapping of an instance of {@code Ref<CompletableFuture<T>>}
     * as a more handy instance of {@code RefFuture<T>}.
     * All methods of the returned RefFuture delegate to the original Ref.
     *
     * Note, that {@code RefFuture<T>} is a sub-interface of
     * {@code Ref<CompletableFuture<T>>}.
     *
     *
     * @param <T>
     * @param delegate
     * @return
     */
    public static <T> RefFuture<T> wrap(Ref<CompletableFuture<T>> delegate) {
        return new RefFutureImpl<T>(delegate);
    }


    /** Wrap an existing ref with completed future */
    public static <T> RefFuture<T> fromRef(Ref<T> ref) {
        RefFuture<T> result = RefFutureImpl.fromFuture(CompletableFuture.completedFuture(ref), ref.getSynchronizer());
        return result;
    }


    /** Create a ref that upon close cancels the future or closes the ref when it is available s*/
    public static <T> RefFuture<T> fromFuture(CompletableFuture<Ref<T>> future, Object synchronizer) {
      return wrap(RefImpl.create(future.thenApply(Ref::get), synchronizer, () -> cancelFutureOrCloseRef(future), null));
    }

    public static void cancelFutureOrCloseRef(CompletableFuture<? extends Ref<?>> future) {
        cancelFutureOrCloseValue(future, Ref::close);
    }


    public static <T> void cancelFutureOrCloseValue(CompletableFuture<T> future, Consumer<? super T> valueCloseAction) {
        try {
            boolean isCancelled = future.cancel(true);
            // logger.debug("isCancelled: " + isCancelled);
            if (!isCancelled) {
                T value = future.get();
                if (value != null && valueCloseAction != null) {
                    valueCloseAction.accept(value);
                }
            }
        } catch (CancellationException | InterruptedException | ExecutionException e) {
            logger.warn("Exception raised during close", e);
        }
    }




    /**
     * Wrap a Ref<RefFuture<T>> as a RefFuture<T>.
     * This creates a new Ref that closes the delegate on close.
     * This method does not acquire the delegate, so after wrapping
     * the delegate should be considered as 'owned' by this wrapper;
     * the delegate should no longer be closed directly.
     *
     */
    public static <T> RefFuture<T> wrap2(Ref<? extends CompletableFuture<Ref<T>>> delegate) {
        CompletableFuture<T> x = delegate.get().thenApply(Ref::get);
        Ref<CompletableFuture<T>> newRef = RefImpl.create(x, delegate.getSynchronizer(), delegate::close);
        return RefFutureImpl.wrap(newRef);
    }

    public static <T> RefFuture<T> wrap3(Ref<? extends Ref<? extends CompletableFuture<T>>> delegate) {
        Object synchronizer = delegate.getSynchronizer();
        CompletableFuture<T> x = delegate.get().get();
        Ref<CompletableFuture<T>> newRef = RefImpl.create(x, synchronizer, delegate::close);
        return RefFutureImpl.wrap(newRef);
    }


}