package org.aksw.commons.util.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;


public class CompletionTracker
    implements Executor
{
    protected Executor executor;

    protected volatile long counter = 0;
    protected Map<Long, CompletableFuture<?>> pending = new HashMap<>();
    protected volatile boolean isShutDown = false;

    // Use locks?
//    protected Lock lock = new ReentrantLock();
//    protected Condition terminated = lock.newCondition();

    public CompletionTracker(Executor executor) {
        super();
        this.executor = executor;
    }

    public static CompletionTracker from(Executor executor) {
        return new CompletionTracker(executor);
    }

    public void shutdown() {
        synchronized (this) {
            this.isShutDown = true;

            checkTerminationCondition();
        }
    }

    public void execute(Runnable runnable) {
        long id;
        CompletableFutureDelegate<Object> exposedFuture;

        synchronized (this) {
            if (isShutDown) {
                throw new RejectedExecutionException("New task rejected because completionTracker was already shut down");
            }

            // Register a completable future in this synchronized block
            // in order to make it immediately visible to any concurrent lookup
            // (especially for whether everything has completed)
            // but start the actual execution only outside of this block
            id = counter++;
            exposedFuture = new CompletableFutureDelegate<>();
            pending.put(id, exposedFuture);
        }

        CompletableFuture<?> internalFuture = CompletableFuture.runAsync(runnable, executor);
        exposedFuture.setDelegate(internalFuture);
        internalFuture.whenComplete((value, throwable) -> {
            synchronized (this) {
                pending.remove(id);
                checkTerminationCondition();
            }

            if (throwable != null) {
                exposedFuture.completeExceptionally(throwable);
            } else {
                exposedFuture.complete(value);
            }
        });

    }

    protected boolean isTerminated() {
        boolean result = isShutDown && pending.isEmpty();
        // System.out.println("result: " + result);
        return result;
    }

    protected void checkTerminationCondition() {
        if (isTerminated()) {
            this.notifyAll();
            // terminated.signal();
        }
    }

    public synchronized void awaitTermination() throws InterruptedException {
        //terminated.await();
        synchronized (this) {
            while (!isTerminated()) {
                wait();
    //            try {
    //            } catch (InterruptedException e) {
    //                throw new RuntimeException(e);
    //            }
            }
        }
    }
}
