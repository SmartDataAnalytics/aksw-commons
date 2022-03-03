package org.aksw.commons.cache.plain;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefImpl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

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
public class ClaimingCache<K, V> {
    protected LoadingCache<K, Ref<V>> cache;
    protected Map<K, Ref<Ref<V>>> claimed;

    public static <K, V> ClaimingCache<K, V> create(
            CacheBuilder<Object, Object> cacheBuilder,
            Function<K, Ref<V>> cacheLoader,
            RemovalListener<K, Ref<V>> removalListener) {
        return new ClaimingCache<>(cacheBuilder, cacheLoader, removalListener);
    }


    public ClaimingCache(
            CacheBuilder<Object, Object> cacheBuilder,
            Function<K, Ref<V>> cacheLoader,
            RemovalListener<K, Ref<V>> removalListener
            ) {
        this.cache = cacheBuilder
            .removalListener(new RemovalListener<K, Ref<V>>() {
                @Override
                public void onRemoval(RemovalNotification<K, Ref<V>> notification) {
                    // Close the reference that is being removed from the cache
                    Ref<V> primaryRef = notification.getValue();
                    primaryRef.close();
                }
            })
            .build(new CacheLoader<K, Ref<V>>() {
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
    public  <V> Ref<V> link(Ref<? extends Ref<V>> refToRef) {
        Ref<? extends Ref<V>> tmpRef = refToRef.acquire();
        return RefImpl.create(tmpRef.get().get(), claimed, tmpRef::close);
    }

    public Ref<V> claimUnsafe(K key) {
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
    public Ref<V> claim(K key) throws ExecutionException {
        Ref<V> result = null;
        Ref<Ref<V>> secondaryRef;

        // Synchronize on 'claimed' because removals can occur asynchronously
        synchronized (claimed) {
            secondaryRef = claimed.get(key);

            if (secondaryRef != null) {
                result = link(secondaryRef); //secondaryRef.acquire();
            }
        }

        if (secondaryRef == null) {
            // Don't block 'claimed' while computing the value
            // Hence, compute the value outside of the synchronized block
            Ref<V> primaryRef = cache.get(key);

            // Put a reference to the value into claimed
            // (if that hasn't happened asynchronously already)
            synchronized (claimed) {
                // Check whether in the meantime the entry has been claimed
                secondaryRef = claimed.get(key);
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
                        cache.put(key, handBackRef);

                        claimed.remove(key);
                        tmpRef.close();
                    }, null);
                    result = link(secondaryRef); //secondaryRef.acquire();
                    secondaryRef.close();

                    claimed.put(key, secondaryRef);
                } else {
                    result = link(secondaryRef); //secondaryRef.acquire();
                }
            }
        }

        return result;
    }

    /** Cannot raise an ExecutionException because it does not trigger loading */
    public Ref<V> claimIfPresent(K key) {
        Ref<V> result = null;
        synchronized (claimed) {
            if (claimed.containsKey(key) || cache.getIfPresent(key) != null) {
                result = claimUnsafe(key);
            }
        }

        return result;
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }
}