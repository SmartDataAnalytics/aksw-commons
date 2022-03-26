package org.aksw.commons.cache.plain;

import java.util.concurrent.ExecutionException;

import org.aksw.commons.util.ref.Ref;

public interface ClaimingCache<K, V> {
    /**
     * Claim a reference to the key's entry.
     *
     * @param key
     * @return
     * @throws ExecutionException
     */
    Ref<V> claim(K key); // throws ExecutionException;

    /** Cannot raise an ExecutionException because it does not trigger loading */
    Ref<V> claimIfPresent(K key);

    /**
     * Get a resource without claiming it. Its cache entry may get evicted any time such that
     * a later invocation of {@link #get(Object)} returns a fresh future
     */
    // CompletableFuture<V> get(K key);

    void invalidateAll();
}
