package org.aksw.commons.cache.async;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefFuture;
import org.aksw.commons.util.ref.RefFutureImpl;
import org.aksw.commons.util.ref.RefImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

/**
 * A wrapper around a cache that on each lookup returns a fresh CompletableFuture that
 * can be cancelled independently. Only if all 'slave' futures are cancelled then the
 * 'master' future is cancelled.
 *
 * Once a master's future is loaded then the slave futures will close themselves
 * as the loading can no longer be interrupted.
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class AsyncRefCache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(AsyncRefCache.class);

    /** The cache with the primary loader */
    protected AsyncLoadingCache<K, V> master;

    /** A 'slave' map that wraps the completable futures of the master as a ref
     * The slave is synchronized with the master - it atomically contains the same keys.
     */
    protected Map<K, RefFuture<V>> slave;

    // protected Consumer<? super V> itemCloser = null;

    /** Whether to cancel loading of items that were unclaimed before loading completed,
     *  if false, the future returned by the master will not be cancelled */
    // protected boolean cancelUnclaimedIncompleteTasks;

    public static <K, V> AsyncRefCache<K, V> create(
            Caffeine<Object, Object> master,
            Function<K, V> cacheLoader,
            RemovalListener<K, V> removalListener) {
        return new AsyncRefCache<>(master, cacheLoader, removalListener);
    }


    public AsyncRefCache(
            Caffeine<Object, Object> cacheBuilder,
            Function<K, V> cacheLoader,
            RemovalListener<K, V> removalListener
            ) {


        this.master = cacheBuilder
            .evictionListener((K key, V value, RemovalCause cause) -> {
                synchronized (slave) {
                    logger.debug("Evicting " + key);
                    removalListener.onRemoval(key, value, cause);
                    slave.remove(key);
                }
            })
            .buildAsync((K key) -> {
                logger.debug("Loading: " + key);
                V value = cacheLoader.apply(key);
                return value;
            });

        slave = new ConcurrentHashMap<>();
    }

    /**
     * Get a fresh complatable future to the key.
     * Cannot be used if a close action on items is configured, because
     * completion of the future would close the item.
     *
     */
    public CompletableFuture<V> getAsCompletableFuture(K key) {
//        if (itemCloser != null) {
//            throw new RuntimeException("A close action for items is configured which prevents CompletableFuture views");
//        }

        RefFuture<V> refFuture = getAsRefFuture(key);

        CompletableFuture<V> result = new CompletableFuture<V>() {
            public boolean cancel(boolean mayInterruptIfRunning) {
                // Future<?> f = Scheduler.systemScheduler().schedule(ForkJoinPool.commonPool(), () -> {}, 500, TimeUnit.MILLISECONDS);

                // The future might have just completed
                if (refFuture.isAlive()) {
                    refFuture.close();
                }
                return super.cancel(mayInterruptIfRunning);
            };
        };
//        .whenComplete((v, t) -> {
//            refFuture.close();
//        });

        refFuture.get().whenComplete((v, t) -> {
            if (t == null) {
                result.complete(v);
            } else {
                result.completeExceptionally(t);
            }

            if (refFuture.isAlive()) {
                refFuture.close();
            }
        });

        return result;
    }

    public void put(K key, RefFuture<V> value) {
        synchronized (slave) {
            if (!value.isAlive()) {
                throw new RuntimeException("Cannot put a dead reference");
            }

            master.put(key, value.get());
            slave.put(key, value);
        }
    }

    /**
     * Get a fresh reference to the item corresponding to 'key'.
     *
     * @param key
     * @return
     */
    public RefFuture<V> getAsRefFuture(K key) {

        RefFuture<V> result;

        synchronized (slave) {

        boolean[] isNewRootRef = { false };
        RefFuture<V> rootRef = slave.computeIfAbsent(key, k -> {
            CompletableFuture<V> future = master.get(key);

            isNewRootRef[0] = true;

            // RefFutureImpl.fromFuture(future, slave);

            Ref<CompletableFuture<V>> tmp = RefImpl.create(future, slave, () -> {
                RefFutureImpl.cancelFutureOrCloseValue(future, null);
                slave.remove(key);
            });
            RefFuture<V> r = RefFutureImpl.wrap(tmp);

            return r;
        });

        result = rootRef.acquire();

        if (isNewRootRef[0]) {
            rootRef.close();
        }

        }

        return result;
    }


    public static void main(String[] args) throws InterruptedException, ExecutionException {

        AsyncRefCache<String, String> cache = AsyncRefCache.<String, String>create(
                Caffeine.newBuilder()
                .executor(ForkJoinPool.commonPool())
                .maximumSize(1000), key -> {
            String r;
            try {
                Thread.sleep(1000);
                r = "value for " + key;
            } catch (InterruptedException e) {
                e.printStackTrace();
                r = null;
            }
            return r;
        }, (k, v, cause) -> {});

        CompletableFuture<String> future = cache.getAsCompletableFuture("test");
        future.cancel(true);

        List<CompletableFuture<String>> futures = IntStream.range(0, 10)
                .mapToObj(i -> cache.getAsCompletableFuture("test"))
                .collect(Collectors.toList());

        futures.forEach(cf -> cf.cancel(true));

        // System.out.println(future.get());

        System.out.println(cache.getAsCompletableFuture("test").get());
    }

    public RefFuture<V> getIfPresent(K key) {
        RefFuture<V> result = slave.get(key);
        return result;
    }


    public void invalidateAll() {
        master.synchronous().invalidateAll();
    }
}
