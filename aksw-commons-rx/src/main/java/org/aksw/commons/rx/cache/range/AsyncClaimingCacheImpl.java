package org.aksw.commons.rx.cache.range;


import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.commons.accessors.SingleValuedAccessorDirect;
import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefFuture;
import org.aksw.commons.util.ref.RefFutureImpl;
import org.aksw.commons.util.ref.RefImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.Scheduler;


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
public class AsyncClaimingCacheImpl<K, V> implements AsyncClaimingCache<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(AsyncClaimingCacheImpl.class);

    /** The map of claimed items; the value is a reference to the RefFuture of level3
     * Once the outer reference gets closed (i.e. when there are no more claims),
     * the value is immediately added to level2.
     */
    protected Map<K, RefFuture<V>> level1;

    /** If the sync pool is non null, then eviction of an item from the active cache
     *  adds those items to this pool. There, items can be scheduled for syncing
     *  e.g. with the file system. An item can be reactived from the sync poll. */

    /**
     * Items that are no longer claimed are added to level2 from where
     * after a delay - they are atomically evicted from level2 and added to level3.
     * The SingleValuedAccessor wrapper acts as a 'holder' which allows 'resurrecting'
     * items from level2 into level1 without level2's eviction action to act due to setting
     * the held reference to null.
     *
     */
    // protected LoadingCache<K, Ref<CompletableFuture<Ref<V>>>> level2;
    protected Cache<K, SingleValuedAccessor<CompletableFuture<V>>> level2;

    /** The cache with the primary loader */
    // protected AsyncLoadingCache<K, Ref<V>> level3;
    protected AsyncLoadingCache<K, V> level3;

    /** The listener for when an item is removed from *all* levels of the cache */
    // protected RemovalListener<K, V> removalListener;

    /** Whether to cancel loading of items that were unclaimed before loading completed,
     *  if false, the future returned by level3 will not be cancelled */
    // protected boolean cancelUnclaimedIncompleteTasks;

    public static <K, V> AsyncClaimingCache<K, V> create(
            Duration syncDelayDuration,
            // Caffeine<Object, Object> cacheBuilder,
            // AsyncRefCache<K, V> level3,

            Caffeine<Object, Object> level3Master,
            Function<K, V> level3CacheLoader,
            RemovalListener<K, V> level3RemovalListener,

            RemovalListener<K, V> level2RemovalListener) {
        return new AsyncClaimingCacheImpl<>(level3Master, level3CacheLoader, level3RemovalListener, level2RemovalListener, syncDelayDuration);
    }


    public AsyncClaimingCacheImpl(
            Caffeine<Object, Object> level3Master,
            Function<K, V> level3CacheLoader,
            RemovalListener<K, V> level3RemovalListener,
            RemovalListener<K, V> level2RemovalListener,
            Duration syncDelayDuration
            ) {

        this.level2 = Caffeine.newBuilder()
                .scheduler(Scheduler.systemScheduler())
                // .expireAfterWrite(1, TimeUnit.SECONDS)
                .expireAfterWrite(syncDelayDuration)
                .evictionListener((K key, SingleValuedAccessor<CompletableFuture<V>> holder, RemovalCause removalCause) -> {
                    CompletableFuture<V> future = holder.get();
                    if (future != null) {
                        logger.trace("Level2 eviction action: Syncing & passing to level 3: " + key);
                        if (future.isDone()) {
                            V value;
                            try {
                                value = future.get();
                            } catch (InterruptedException | ExecutionException e) {
                                throw new RuntimeException(e);
                            }
                            level2RemovalListener.onRemoval(key, value, removalCause);

                            // RefFuture<V> tmpRef = refFuture.acquire();
                            level3.put(key, future);
                            // tmpRef.close();
                        }
                    } else {
                        logger.trace("Level2 eviction action: Reference was null - assuming re-claimed to level 1");
                    }

                    // RefFuture<V> refFuture = RefFutureImpl.fromRef(primaryRef);
//
//                    Ref<V> handBackRef = primaryRef.getRootRef().acquire();
//                    level3.put(key, CompletableFuture.completedFuture(handBackRef));

                    // level3.put(key, CompletableFuture.completedFuture(primaryRef));
                    // primaryRef.close();
                })
                .build();
                // .build(key -> new SingleValuedAccessor<>(level3.getAsRefFuture(key)));

        // Configure the level3 eviction listener such that the custom listener is only
        // invoked if a key is not present in any other levels of the cache

        this.level3 = level3Master
                .evictionListener((K key, V value, RemovalCause removalCause) -> {
                    synchronized (level1) {
                        SingleValuedAccessor<CompletableFuture<V>> holder = level2.getIfPresent(key);
                        boolean isKeyPresentInLevel1Or2 = level1.containsKey(key) || (holder != null && holder.get() != null);

                        if (!isKeyPresentInLevel1Or2) {
                            logger.debug("Complete eviction of " + key + " is - no longer present in any other level");
                            level3RemovalListener.onRemoval(key, value, removalCause);
                        }
                    }
                })
                .buildAsync((K key) -> {
                    logger.debug("Loading: " + key);
                    V value = level3CacheLoader.apply(key);
                    return value;
                });


//        this.level3 = new AsyncRefCache<>(
//                level3Master,
//                level3CacheLoader,
//                (K key, V value, RemovalCause removalCause) -> {
//                    synchronized (level1) {
//                        SingleValuedAccessor<RefFuture<V>> holder = level2.getIfPresent(key);
//                        boolean isKeyPresentInLevel1Or2 = level1.containsKey(key) || (holder != null && holder.get() != null);
//
//                        if (!isKeyPresentInLevel1Or2) {
//                            logger.debug("Complete eviction of " + key + " is - no longer present in any other level");
//                            level3RemovalListener.onRemoval(key, value, removalCause);
//                        }
//                    }
//                });

//
//        this.level3 = cacheBuilder
//            .removalListener(new RemovalListener<K, Ref<V>>() {
//                @Override
//                public void onRemoval(K key, Ref<V> primaryRef, RemovalCause cause) {
//                    logger.debug("Level3: Closed: " + key);
//                    primaryRef.close();
//                }
//            })
//            .buildAsync(new CacheLoader<K, Ref<V>>() {
//                @Override
//                public Ref<V> load(K key) throws Exception {
//                    logger.debug("Level3: Loading: " + key);
//                    // If the reference is still in the claimed map then
//                    // re-acquire it (without having to actually load anything)
//
//                    Ref<V> rootRef = null;
//                    synchronized (level1) {
//                        Ref<CompletableFuture<Ref<V>>> secondaryRef = level1.get(key);
//                        if (secondaryRef != null) {
//                            // The prior future must have completed; otherwise this load method could not be called
//                            rootRef = secondaryRef.get().get().acquire();
//                        }
//                    }
//
//                    if (rootRef == null) {
//                        rootRef = cacheLoader.apply(key);
//                    }
//
//                    return rootRef;
//                }
//            });

        level1 = new ConcurrentHashMap<>();
    }


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
    @Override
    public RefFuture<V> claim(K key) {

        RefFuture<V> result;

        // Probably we need this synchronized block because to avoid concurrency issue
        // with the close action of a Reference
        synchronized (level1) {

        // The block below atomically creates the root ref for the key as needed
        // Any further claimInternal call acquires a child ref from the root ref
        // The invocation of claimInternal that triggers computeIfAbsent
        // acquires a child ref of the root one and closes the root.

        boolean[] isFreshSecondaryRef = { false };
        RefFuture<V> secondaryRef = level1.computeIfAbsent(key, k -> {
            // Wrap the loaded reference such that closing the fully loaded reference adds it to level 2

            SingleValuedAccessor<CompletableFuture<V>> holder = level2.getIfPresent(key);
            CompletableFuture<V> tmpRefFuture = holder == null ? null : holder.get();
            if (tmpRefFuture != null) {
                logger.trace("Claiming item [" + key + "] from level2");
                // Unset the value of the holder such that invalidation does not apply the close action
                holder.set(null);
                level2.invalidate(key);
            } else {
                logger.trace("Claiming item [" + key + "] from level3");
                tmpRefFuture = level3.get(key);
            }


            final CompletableFuture<V> refFuture = tmpRefFuture;
            Ref<CompletableFuture<V>> freshSecondaryRef =
                    RefImpl.create(tmpRefFuture, level1, () -> {
                        RefFutureImpl.cancelFutureOrCloseValue(refFuture, null);
                        level1.remove(key);
                        logger.trace("Item [" + key + "] was unclaimed. Transferring to level2.");
                        level2.put(key, new SingleValuedAccessorDirect<>(refFuture));
                    });
            isFreshSecondaryRef[0] = true;

            return RefFutureImpl.wrap(freshSecondaryRef);
        });

        result = secondaryRef.acquire();
        if (isFreshSecondaryRef[0]) {
            secondaryRef.close();
        }

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
//    protected Ref<V> postProcessLoadedItem(K key, Ref<V> primaryRef) {
//        Ref<V> result = null;
//        // Put a reference to the value into claimed
//        // (if that hasn't happened asynchronously already)
//        synchronized (level1) {
//            // Check whether in the meantime the entry has been claimed
//            Ref<RefFuture<V>> secondaryRef = level1.get(key);
//            if (secondaryRef == null) {
//                // Note that the root ref is synchronized on 'claimed' as well
//                // Hence, if the ref had been released then claimed.get(key) would have yeld null
//
//                // Ref<V> secondaryRef = primaryRef.acquire("rootClaim");
//                Ref<V> tmpRef = primaryRef.acquire();
//                secondaryRef = RefImpl.create(primaryRef, level1, () -> {
//                    // Hand back the value to the cache
//                    // If the value is already in the cache it will get removed / released
//                    // so we need to create yet another helper reference
//                    Ref<V> handBackRef = primaryRef.getRootRef().acquire();
//                    level2.put(key, CompletableFuture.completedFuture(handBackRef));
//
//                    level1.remove(key);
//                    tmpRef.close();
//                }, null);
//                result = hideInnerRef(secondaryRef); //secondaryRef.acquire();
//                secondaryRef.close();
//
//                level1.put(key, secondaryRef);
//            } else {
//                result = hideInnerRef(secondaryRef); //secondaryRef.acquire();
//            }
//        }
//        return result;
//    }

    /** Cannot raise an ExecutionException because it does not trigger loading */
    @Override
    public RefFuture<V> claimIfPresent(K key) {
        RefFuture<V> result = null;
        synchronized (level1) {
            if (level1.containsKey(key) || level2.getIfPresent(key) != null || level3.getIfPresent(key) != null) {
                result = claimUnsafe(key);
            }
        }

        return result;
    }

    @Override
    public void invalidateAll() {
         level3.synchronous().invalidateAll();
    }




    // Some ugliness to turn a Ref<Ref<V>> into a Ref<V>
//    public  <V> Ref<V> link(Ref<? extends Ref<V>> refToRef) {
//        Ref<? extends Ref<V>> tmpRef = refToRef.acquire();
//        return RefImpl.create(tmpRef.get().get(), claimed, tmpRef::close);
//    }

    public static <V> Ref<V> hideInnerRef(Ref<? extends Ref<V>> refToRef, Object synchronizer) {
        Ref<? extends Ref<V>> tmpRef = refToRef.acquire();
        return RefImpl.create(tmpRef.get().get(), synchronizer, tmpRef::close);
    }

    public Ref<V> hideInnerRef(Ref<? extends Ref<V>> refToRef) {
        return hideInnerRef(refToRef, level1);
    }

}




//
//        RefFuture<V> result = null;
//        Ref<RefFuture<V>> secondaryRef;
//
//        // Synchronize on 'claimed' because removals can occur asynchronously
//        synchronized (level1) {
//            secondaryRef = level1.get(key);
//
//            if (secondaryRef != null) {
////                CompletableFuture<Ref<V>> resolvedFuture = CompletableFuture.completedFuture(secondaryRef.acquire());
//                // Ref<V> tmp = hideInnerRef(secondaryRef);
//                // result = RefFutureImpl.fromFuture(resolvedFuture, claimed); // , resolvedFuture)// hideInnerRef(secondaryRef); //secondaryRef.acquire();
//                // result = RefFutureImpl.fromRef(tmp);
//                result = RefFutureImpl.wrap3(secondaryRef.acquire());
//            }
//        }
//
//        if (secondaryRef == null) {
//            // Don't block 'level1' while computing the value asynchronously
//            // Hence, compute the value outside of the synchronized block
//            CompletableFuture<Ref<V>> tmpRef = level3.get(key)
//                    .thenApply(item -> postProcessLoadedItem(key, item));
//
//            result = RefFutureImpl.fromFuture(tmpRef, level1);
//        }
//
//        return result;

//CompletableFuture<Ref<V>> primaryRefFuture = rootRefFuture
//.thenApply(rootRef -> {
//
//  Ref<V> putBackGuardRef = rootRef.acquire();
//  Ref<Ref<V>> primaryRef = RefImpl.create(rootRef, level1, () -> {
//      level3.put(key, CompletableFuture.completedFuture(rootRef));
//      putBackGuardRef.close();
//  }, null);
//  putBackGuardRef.close();
//
//  return rootRef;
//});
