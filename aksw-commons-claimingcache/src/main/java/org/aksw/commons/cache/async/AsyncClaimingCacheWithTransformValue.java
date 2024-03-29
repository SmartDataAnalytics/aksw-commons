package org.aksw.commons.cache.async;

import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.commons.util.closeable.Disposable;
import org.aksw.commons.util.ref.RefFuture;

public class AsyncClaimingCacheWithTransformValue<K, V1, V2>
    implements AsyncClaimingCache<K, V2>
{
    protected AsyncClaimingCache<K, V1> delegate;
    protected Function<? super V1, ? extends V2> transform;

    public AsyncClaimingCacheWithTransformValue(AsyncClaimingCache<K, V1> delegate,
            Function<? super V1, ? extends V2> transform) {
        super();
        this.delegate = delegate;
        this.transform = transform;
    }

    public static <K, V1, V2> AsyncClaimingCache<K, V2> create(AsyncClaimingCache<K, V1> delegate,  Function<? super V1, ? extends V2> transform) {
        return new AsyncClaimingCacheWithTransformValue<>(delegate, transform);
    }

    @Override
    public RefFuture<V2> claim(K key) { // throws ExecutionException {
        RefFuture<V1> ref = delegate.claim(key);
        RefFuture<V2> result = ref.acquireTransformedAndCloseThis(transform);
        return result;
    }

    @Override
    public RefFuture<V2> claimIfPresent(K key) {
        RefFuture<V1> ref = delegate.claimIfPresent(key);
        RefFuture<V2> result = ref == null ? null : ref.acquireTransformedAndCloseThis(transform);
        return result;
    }

    @Override
    public void invalidateAll() {
        delegate.invalidateAll();
    }

    @Override
    public Disposable addEvictionGuard(Predicate<? super K> predicate) {
        return delegate.addEvictionGuard(predicate);
    }
}
