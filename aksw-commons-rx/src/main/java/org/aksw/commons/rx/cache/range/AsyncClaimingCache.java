package org.aksw.commons.rx.cache.range;

import java.util.concurrent.ExecutionException;

import org.aksw.commons.util.ref.RefFuture;

public interface AsyncClaimingCache<K, V> {

    /**
     * Claim a reference to the key's entry.
     *
     * @param key
     * @return
     * @throws ExecutionException
     */
    RefFuture<V> claim(K key); // throws ExecutionException;

    /** Cannot raise an ExecutionException because it does not trigger loading */
    RefFuture<V> claimIfPresent(K key);

    void invalidateAll();

}