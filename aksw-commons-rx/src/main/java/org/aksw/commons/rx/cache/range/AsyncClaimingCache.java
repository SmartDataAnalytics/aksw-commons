package org.aksw.commons.rx.cache.range;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefFuture;
import org.aksw.commons.util.ref.RefFutureImpl;
import org.aksw.commons.util.ref.RefImpl;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;


/**
 * An extension of loading cache that allows for making explicit
 * references to cached entries such that they won't be evicted.
 *
 * Uses 'smart' references.
 *
 * The cache:
 *   Upon load, the cache first checks whether for the key there is already a claimed reference.
 *   Upon load it references its own item, and releases this reference upon removal.
 * Claiming: Claiming an item acquires an additional reference and adds that reference to the claimed map.
 * When a claimed item is no longer referenced, the item is put back to cache.
 *
 *
 *
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class AsyncClaimingCache<K, V> {
    /** The cache with the primary loader */
    protected AsyncLoadingCache<K, Ref<V>> activeCache;

    /** If the sync pool is non null, then eviction of an item from the active cache
     *  adds those items to this pool. There, items can be scheduled for syncing
     *  e.g. with the file system. An item can be reactived from the sync poll. */
    protected Cache<K, V> syncPool;


    // Only successfully loaded items can be added into the claimed cache
    protected Map<K, Ref<Ref<V>>> claimed;

    public static <K, V> AsyncClaimingCache<K, V> create(
            Caffeine<Object, Object> cacheBuilder,
            Function<K, Ref<V>> cacheLoader,
            RemovalListener<K, Ref<V>> removalListener) {
        return new AsyncClaimingCache<>(cacheBuilder, cacheLoader, removalListener);
    }


    public AsyncClaimingCache(
            Caffeine<Object, Object> cacheBuilder,
            Function<K, Ref<V>> cacheLoader,
            RemovalListener<K, Ref<V>> removalListener
            ) {
        this.activeCache = cacheBuilder
            .removalListener(new RemovalListener<K, Ref<V>>() {
                @Override
                public void onRemoval(K key, Ref<V> primaryRef, RemovalCause cause) {
                    primaryRef.close();
                }
            })
            .buildAsync(new CacheLoader<K, Ref<V>>() {
                @Override
                public Ref<V> load(K key) throws Exception {
                    // If the reference is still in the claimed map then
                    // re-acquire it (without having to actually load anything)
                    Ref<V> primaryRef = null;
                    synchronized (claimed) {
                        Ref<Ref<V>> secondaryRef = claimed.get(key);
                        if (secondaryRef != null) {
                            // Acquire claimed's root ref for the cache
                            primaryRef = secondaryRef.get().acquire(); // claimedRef.getRootRef().acquire("cache");
                        }
                    }

                    if (primaryRef == null) {
                        primaryRef = cacheLoader.apply(key);
                    }

                    return primaryRef;
                }
            });

        claimed = new HashMap<>();
    }


    // Some ugliness to turn a Ref<Ref<V>> into a Ref<V>
//    public  <V> Ref<V> link(Ref<? extends Ref<V>> refToRef) {
//        Ref<? extends Ref<V>> tmpRef = refToRef.acquire();
//        return RefImpl.create(tmpRef.get().get(), claimed, tmpRef::close);
//    }

        public  <V> Ref<V> hideInnerRef(Ref<? extends Ref<V>> refToRef) {
            Ref<? extends Ref<V>> tmpRef = refToRef.acquire();
            return RefImpl.create(tmpRef.get().get(), claimed, tmpRef::close);
        }


//    public  <V> Ref<CompletableFuture<V>> hideInnerRef(Ref<? extends CompletableFuture<? extends Ref<V>>> refToRef) {
//        Ref<? extends CompletableFuture<? extends Ref<V>>> tmpRef = refToRef.acquire();
//        return RefImpl.create(tmpRef.get().thenApply(Ref::get), claimed, tmpRef::close);
//    }

//    public  <V> RefFuture<V> hideInnerRef(Ref<? extends CompletableFuture<? extends Ref<V>>> refToRef) {
//        Ref<? extends CompletableFuture<? extends Ref<V>>> tmpRef = refToRef.acquire();
//        return RefImpl.create(tmpRef.get().thenApply(Ref::get), claimed, tmpRef::close);
//    }


    public RefFuture<V> claimUnsafe(K key) {
        try {
            return claim(key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Claim a reference to the key's entry.
     *
     * @param key
     * @return
     * @throws ExecutionException
     */
    public RefFuture<V> claim(K key) throws ExecutionException {
        RefFuture<V> result = null;
        Ref<Ref<V>> secondaryRef;

        // Synchronize on 'claimed' because removals can occur asynchronously
        synchronized (claimed) {
            secondaryRef = claimed.get(key);

            if (secondaryRef != null) {
//                CompletableFuture<Ref<V>> resolvedFuture = CompletableFuture.completedFuture(secondaryRef);
//                result = RefFutureImpl.fromFuture(resolvedFuture, claimed); // , resolvedFuture)// hideInnerRef(secondaryRef); //secondaryRef.acquire();

            }
        }

        if (secondaryRef == null) {
            // Don't block 'claimed' while computing the value asynchronously
            // Hence, compute the value outside of the synchronized block
            CompletableFuture<Ref<V>> tmp = activeCache.get(key)
                    .thenApply(item -> postProcessLoadedItem(key, item));

            result = RefFutureImpl.fromFuture(tmp, claimed);
        }

        return result;
    }


    /**
     * Once the primary ref is delivered by the cache, it is
     * claimed. Claiming creates a secondary reference.
     *
     * @param key
     * @param primaryRef
     * @return
     */
    protected Ref<V> postProcessLoadedItem(K key, Ref<V> primaryRef) {
        Ref<V> result = null;
        // Put a reference to the value into claimed
        // (if that hasn't happened asynchronously already)
        synchronized (claimed) {
            // Check whether in the meantime the entry has been claimed
            Ref<Ref<V>> secondaryRef = claimed.get(key);
            if (secondaryRef == null) {
                // Note that the root ref is synchronized on 'claimed' as well
                // Hence, if the ref had been released then claimed.get(key) would have yeld null

                // Ref<V> secondaryRef = primaryRef.acquire("rootClaim");
                Ref<V> tmpRef = primaryRef.acquire();
                secondaryRef = RefImpl.create(primaryRef, claimed, () -> {
                    // Hand back the value to the cache
                    // If the value is already in the cache it will get removed / released
                    // so we need to create yet another helper reference
                    Ref<V> handBackRef = primaryRef.getRootRef().acquire();
                    activeCache.put(key, CompletableFuture.completedFuture(handBackRef));

                    claimed.remove(key);
                    tmpRef.close();
                }, null);
                result = hideInnerRef(secondaryRef); //secondaryRef.acquire();
                secondaryRef.close();

                claimed.put(key, secondaryRef);
            } else {
                result = hideInnerRef(secondaryRef); //secondaryRef.acquire();
            }
        }
        return result;
    }

    /** Cannot raise an ExecutionException because it does not trigger loading */
    public RefFuture<V> claimIfPresent(K key) {
        RefFuture<V> result = null;
        synchronized (claimed) {
            if (claimed.containsKey(key) || activeCache.getIfPresent(key) != null) {
                result = claimUnsafe(key);
            }
        }

        return result;
    }

    public void invalidateAll() {
        activeCache.synchronous().invalidateAll();
    }
}