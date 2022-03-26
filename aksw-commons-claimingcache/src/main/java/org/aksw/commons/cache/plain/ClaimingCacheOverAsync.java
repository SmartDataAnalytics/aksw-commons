package org.aksw.commons.cache.plain;

import org.aksw.commons.cache.async.AsyncClaimingCache;
import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefFuture;
import org.aksw.commons.util.ref.RefImpl;

public class ClaimingCacheOverAsync<K, V>
    implements ClaimingCache<K, V>
{
    protected AsyncClaimingCache<K, V> delegate;

    public ClaimingCacheOverAsync(AsyncClaimingCache<K, V> delegate) {
        super();
        this.delegate = delegate;
    }

    public static <K, V> ClaimingCache<K, V> wrap(AsyncClaimingCache<K, V> delegate) {
        return new ClaimingCacheOverAsync<>(delegate);
    }

    @Override
    public Ref<V> claim(K key) {
        RefFuture<V> refFuture = delegate.claim(key);
        return await(refFuture);
    }

    protected Ref<V> await(RefFuture<V> refFuture) {
        V value = refFuture.await();
        Ref<V> result = RefImpl.create(value, refFuture.getSynchronizer(), refFuture::close);
        return result;
    }

    @Override
    public Ref<V> claimIfPresent(K key) {
        RefFuture<V> refFuture = delegate.claimIfPresent(key);
        Ref<V> result = refFuture == null ? null : await(refFuture);
        return result;
    }

    @Override
    public void invalidateAll() {
        delegate.invalidateAll();
    }
}
