package org.aksw.commons.rx.cache.range;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.aksw.commons.util.ref.RefFuture;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;

public class AsyncClaimingCacheTests {

    private static final Logger logger = LoggerFactory.getLogger(AsyncClaimingCacheTests.class);


    public AsyncClaimingCache<String, String> createTestCache() {
        AsyncClaimingCache<String, String> result = AsyncClaimingCache.create(
                Duration.ofSeconds(1),

                Caffeine.newBuilder()
                        .scheduler(Scheduler.systemScheduler())
                        .expireAfterWrite(1, TimeUnit.SECONDS),
                        key -> {
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            return "valueOf[" + key + "]";
                        },
                        (key, value, cause) -> {},

                (key, value, cause) -> {});

        return result;
    }

    @Test
    public void test1() throws ExecutionException, InterruptedException {

        AsyncClaimingCache<String, String> cache = createTestCache();
        RefFuture<String> future = cache.claim("hello");
        System.out.println(future.await());


        future.acquire().close();
        future.close();

        Thread.sleep(3000);
        logger.debug("Main thread done");

        // AsyncClaimingCache.create(null, null, null)

    }
}
