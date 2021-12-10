package org.aksw.commons.rx.cache.range;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefFuture;
import org.aksw.commons.util.ref.RefFutureImpl;
import org.aksw.commons.util.ref.RefImpl;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;

public class CaffeineExperiments {


    public static void main(String[] args) throws InterruptedException, ExecutionException {
        AsyncLoadingCache<String, Ref<String>> cache = Caffeine.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(1, TimeUnit.SECONDS)
        .evictionListener((String key, Ref<String> ref, RemovalCause cause) -> {
            System.out.println("Evicted: " + key);
            ref.close();
        })
        .buildAsync(key -> {
            Ref<String> r;
            try {
                System.out.println("Loading: " + key);
                Thread.sleep(1000);
                System.out.println("Loaded: " + key);
                r = RefImpl.create(key, null, () -> {
                    System.out.println("Released: " + key);
                }, null);
            //} catch (CancellationException | InterruptedException e) {
            } catch (Exception e) {
                System.out.println("loading interrupted");
                r = null;
            }
            return r;
        });


        CompletableFuture<Ref<String>> page1 = cache.get("page1");
        CompletableFuture<Ref<String>> page2 = cache.get("page2");
        CompletableFuture<Ref<String>> page3 = cache.get("page3");
        page2.get().close();
        page3.get().close();
        Thread.sleep(500);
        // CompletableFuture<String> future2 = cache.get("test");
        RefFuture<String> ref1 = RefFutureImpl.fromFuture(page1, null);
        RefFuture<String> ref2 = ref1.acquire();

        ref1.close();
        ref2.close();

        System.out.println("going to sleep");

        Thread.sleep(5000);
        System.out.println("done sleeping");
//        System.out.println(future);
//        System.out.println(future2);
//
//        future.cancel(true);
//        String value = future.get();
//        System.out.println(value);


    }
}
