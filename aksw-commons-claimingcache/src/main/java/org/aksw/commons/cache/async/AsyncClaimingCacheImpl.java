package org.aksw.commons.cache.async;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.commons.accessors.SingleValuedAccessorDirect;
import org.aksw.commons.util.closeable.Disposable;
import org.aksw.commons.util.lock.LockUtils;
import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefFuture;
import org.aksw.commons.util.ref.RefFutureImpl;
import org.aksw.commons.util.ref.RefImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.Scheduler;


/**
 * Implementation of async claiming cache.
 * Claimed entries will never be evicted. Conversely, unclaimed items remain are added to a cache such that timely re-claiming
 * will be fast.
 *
 * Use cases:
 * - Resource sharing: Ensure that the same resource is handed to all clients requesting one by key.
 * - Resource pooling: Claimed resources will never be closed, but unclaimed resources (e.g. something backed by an input stream)
 *   may remain on standby for a while.
 *
 * Another way to view this class is as a mix of a map with weak values and a cache.
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class AsyncClaimingCacheImpl<K, V>
    implements AsyncClaimingCache<K, V>
{
    private static final Logger logger = LoggerFactory.getLogger(AsyncClaimingCacheImpl.class);

    // level1: claimed items - those items will never be evicted as long as the references are not closed
    protected Map<K, RefFuture<V>> level1;

    // level2: the caffine cache - items in this cache are not claimed are subject to eviction according to configuration
    protected AsyncLoadingCache<K, V> level2;

    // level3: items evicted from level2 but caught be eviction protection
    protected Map<K, V> level3;

    // Runs atomically in the claim action after the entry exists in level1
    protected BiConsumer<K, RefFuture<V>> claimListener;

    // Runs atomically in the unclaim action before the entry is removed from level1
    protected BiConsumer<K, RefFuture<V>> unclaimListener;

    // A lock that prevents invalidation while entries are being loaded
    protected ReentrantReadWriteLock invalidationLock = new ReentrantReadWriteLock();

    protected LinkedList<Predicate<? super K>> evictionGuards;

    protected RemovalListener<K, V> evictionListener;


    public AsyncClaimingCacheImpl(
            Map<K, RefFuture<V>> level1,
            AsyncLoadingCache<K, V> level2,
            Map<K, V> level3,
            LinkedList<Predicate<? super K>> evictionGuards,
            BiConsumer<K, RefFuture<V>> claimListener,
            BiConsumer<K, RefFuture<V>> unclaimListener,
            RemovalListener<K, V> evictionListener) {
        super();
        this.level1 = level1;
        this.level2 = level2;
        this.level3 = level3;
        this.evictionGuards = evictionGuards;
        this.claimListener = claimListener;
        this.unclaimListener = unclaimListener;
        this.evictionListener = evictionListener;
    }

    // Inner class use to synchronize per-key access
    private static class Latch {
        // A flag to indicate that removal of the corresponding entry from keyToSynchronizer needs to be prevented
        // because another thread already started reusing this latch
        volatile int numWaitingThreads = 1;

        Latch inc() { ++numWaitingThreads; return this; }
        Latch dec() { --numWaitingThreads; return this; }
        int get() { return numWaitingThreads; }

        @Override
        public String toString() {
            return "Latch " + System.identityHashCode(this) + " has "+ numWaitingThreads + " threads waiting";
        }
    }

    protected Map<K, Latch> keyToSynchronizer = new ConcurrentHashMap<>();

// TODO claiming removes the entry from the cache
//	@Override
//	public CompletableFuture<V> get(K key) {
//		return level2.get(key);
//	}


    /**
     * Registers a predicate that 'caches' entries about to be evicted
     * When closing the registration then keys that have not moved back into the ache
     * by reference will be immediately evicted.
     */
    @Override
    public Disposable addEvictionGuard(Predicate<? super K> predicate) {
        ListIterator<?> removalPointer;
        synchronized (evictionGuards) {
            evictionGuards.add(predicate);
            removalPointer = evictionGuards.listIterator(evictionGuards.size());
            removalPointer.previous();
        }

        return () -> {
            synchronized (evictionGuards) {
                removalPointer.remove();
                runLevel3Eviction();
            }
        };
    }

    protected void runLevel3Eviction() {
        for (Entry<K, V> e : level3.entrySet()) {
            K k = e.getKey();
            V v = e.getValue();
            boolean isGuarded = evictionGuards.stream().anyMatch(p -> p.test(k));
            if (!isGuarded) {
                evictionListener.onRemoval(k, v, RemovalCause.COLLECTED);
            }
        }
    }



    public RefFuture<V> claim(K key) {
        RefFuture<V> result;

        // We rely on ConcurrentHashMap.compute operating atomically
        Latch synchronizer = keyToSynchronizer.compute(key, (k, before) -> before == null ? new Latch() : before.inc());

        // /guarded_entry/ marker; referenced in comment below

        synchronized (synchronizer) {
            keyToSynchronizer.compute(key, (k, before) -> before.dec());

            boolean[] isFreshSecondaryRef = { false };


            // Guard against concurrent invalidations
            RefFuture<V> secondaryRef = LockUtils.runWithLock(invalidationLock.readLock(), () -> {
                return level1.computeIfAbsent(key, k -> {
                    // Wrap the loaded reference such that closing the fully loaded reference adds it to level 2

                    logger.trace("Claiming item [" + key + "] from level2");
                    CompletableFuture<V> future = level2.get(key);
                    level2.asMap().remove(key);


                    SingleValuedAccessor<RefFuture<V>> holder = new SingleValuedAccessorDirect<>(null);

                    Ref<CompletableFuture<V>> freshSecondaryRef =
                        RefImpl.create(future, synchronizer, () -> {

                            // This is the unclaim action

                            RefFuture<V> v = holder.get();

                            if (unclaimListener != null) {
                                unclaimListener.accept(key, v);
                            }

                            RefFutureImpl.cancelFutureOrCloseValue(future, null);
                            level1.remove(key);
                            logger.trace("Item [" + key + "] was unclaimed. Transferring to level2.");
                            level2.put(key, future);

                            // If there are no waiting threads we can remove the latch
                            keyToSynchronizer.compute(key, (kk, before) -> before.get() == 0 ? null : before);
                        });
                    isFreshSecondaryRef[0] = true;

                    RefFuture<V> r = RefFutureImpl.wrap(freshSecondaryRef);
                    holder.set(r);

                    return r;
                });
            });

            result = secondaryRef.acquire();

            if (claimListener != null) {
                claimListener.accept(key, result);
            }

            if (isFreshSecondaryRef[0]) {
                secondaryRef.close();
            }
        }

        return result;
    }

    public static class Builder<K, V>
    {
        protected Caffeine<Object, Object> caffeine;
        protected CacheLoader<K, V> cacheLoader;
        protected BiConsumer<K, RefFuture<V>> claimListener;
        protected BiConsumer<K, RefFuture<V>> unclaimListener;
        protected RemovalListener<K, V> userEvictionListener;

        Builder<K, V> setCaffeine(Caffeine<Object, Object> caffeine) {
            this.caffeine = caffeine;
            return this;
        }

        public Builder<K, V> setClaimListener(BiConsumer<K, RefFuture<V>> claimListener) {
            this.claimListener = claimListener;
            return this;
        }

        public Builder<K, V> setUnclaimListener(BiConsumer<K, RefFuture<V>> unclaimListener) {
            this.unclaimListener = unclaimListener;
            return this;
        }

        public Builder<K, V> setCacheLoader(CacheLoader<K, V> cacheLoader) {
            this.cacheLoader = cacheLoader;
            return this;
        }

        public Builder<K, V> setEvictionListener(RemovalListener<K, V> evictionListener) {
            this.userEvictionListener = evictionListener;
            return this;
        }

        @SuppressWarnings("unchecked")
        public AsyncClaimingCacheImpl<K, V> build() {

            Map<K, RefFuture<V>> level1 = new ConcurrentHashMap<>();
            Map<K, V> level3 = new ConcurrentHashMap<>();
            LinkedList<Predicate<? super K>> evictionGuards = new LinkedList<>();

            RemovalListener<K, V> level3AwareEvictionListener = (k, v, c) -> {
                boolean isGuarded = false;
                synchronized (evictionGuards) {
                    // Check for an eviction guard
                    for (Predicate<? super K> evictionGuard : evictionGuards) {
                        isGuarded = evictionGuard.test(k);
                        if (isGuarded) {
                            logger.debug("Protecting from eviction: " + k + " - " + level3.size() + " items protected");
                            level3.put(k, v);
                            break;
                        }
                    }
                }

                if (!isGuarded) {
                    if (userEvictionListener != null) {
                        userEvictionListener.onRemoval((K)k, (V)v, c);
                    }
                }
            };

            caffeine.evictionListener((k, v, c) -> {
                K kk = (K)k;
                V vv = (V)v;

                // Check for actual removal - key no longer present in level1
                if (!level1.containsKey(k)) {
                    level3AwareEvictionListener.onRemoval(kk, vv, c);
                }
            });


            // Cache loader that checks for existing items in level
            CacheLoader<K, V> level3AwareCacheLoader = k -> {
                Object[] tmp = new Object[] { null };
                // Atomically get and remove an existing key from level3
                level3.compute(k, (kk, v) -> {
                    tmp[0] = v;
                    return null;
                });

                V r = (V)tmp[0];
                if (r == null) {
                    r = cacheLoader.load(k);
                }
                return r;
            };

            AsyncLoadingCache<K, V> level2 = caffeine.buildAsync(level3AwareCacheLoader);


            return new AsyncClaimingCacheImpl<K, V>(level1, level2, level3, evictionGuards, claimListener, unclaimListener, level3AwareEvictionListener);
        }
    }

    public static <K, V> Builder<K, V> newBuilder(Caffeine<Object, Object> caffeine) {
        Builder<K, V> result = new Builder<>();
        result.setCaffeine(caffeine);
        return result;
    }


    public static void main(String[] args) throws InterruptedException {

        AsyncClaimingCacheImpl<String, String> cache = AsyncClaimingCacheImpl.<String, String>newBuilder(
                Caffeine.newBuilder().maximumSize(10).expireAfterWrite(1, TimeUnit.SECONDS).scheduler(Scheduler.systemScheduler()))
            .setCacheLoader(key -> "Loaded " + key)
            .setEvictionListener((k, v, c) -> System.out.println("Evicted " + k))
            .setClaimListener((k, v) -> System.out.println("Claimed: " + k))
            .setUnclaimListener((k, v) -> System.out.println("Unclaimed: " + k))
            .build();

        RefFuture<String> ref = cache.claim("hell");

        Disposable disposable = cache.addEvictionGuard(k -> k.contains("hell"));

        System.out.println(ref.await());
        ref.close();

        TimeUnit.SECONDS.sleep(5);

        RefFuture<String> reclaim = cache.claim("hell");

        disposable.close();

        reclaim.close();

        TimeUnit.SECONDS.sleep(5);

        System.out.println("done");
    }

    /**
     * Claim a key only if it is already present.
     *
     * This implementation is a best effort approach:
     * There is a very slim chance that just between testing a key for presence and claiming its entry
     * an eviction occurs - causing claiming of a non-present key and thus triggering a load action.
     */
    @Override
    public RefFuture<V> claimIfPresent(K key) {
        RefFuture<V> result = level1.containsKey(key) || level2.asMap().containsKey(key) ? claim(key) : null;
        return result;
    }


    @Override
    public void invalidateAll() {
        LockUtils.runWithLock(invalidationLock.writeLock(), () -> {
            level2.synchronous().invalidateAll();
        });
    }
}
