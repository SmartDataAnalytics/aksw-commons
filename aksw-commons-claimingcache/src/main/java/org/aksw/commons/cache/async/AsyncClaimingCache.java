package org.aksw.commons.cache.async;

import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import org.aksw.commons.util.closeable.Disposable;
import org.aksw.commons.util.ref.RefFuture;

/**
 * Interface for an async cache that allows "claiming" (= making explicit references) to entries.
 * As long as an entry is claimed it will not be evicted.
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
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

    /**
     * Protect eviction of certain keys as long as the guard is not disposed.
     * Disposable may immediately evict all no longer guarded items */
    Disposable addEvictionGuard(Predicate<? super K> predicate);

    /**
     * Get a resource without claiming it. Its cache entry may get evicted any time such that
     * a later invocation of {@link #get(Object)} returns a fresh future
     */
    // CompletableFuture<V> get(K key);

    void invalidateAll();
}
