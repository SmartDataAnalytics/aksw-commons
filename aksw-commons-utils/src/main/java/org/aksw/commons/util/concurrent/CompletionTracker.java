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

    protected long counter = 0;
    protected Map<Long, CompletableFuture<?>> pending = new HashMap<>();
    protected boolean isShutDown = false;

    // Use locks?
//    protected Lock lock = new ReentrantLock();
//    protected Condition terminated = lock.newCondition();

    public CompletionTracker(Executor executor) {
        super();
        this.executor = executor;
    }

    public CompletionTracker create(Executor executor) {
        return new CompletionTracker(executor);
    }

    public void shutdown() {
        synchronized (this) {
            this.isShutDown = true;

            checkTerminationCondition();
        }
    }

    public void execute(Runnable runnable) {
        synchronized (this) {
            if (isShutDown) {
                throw new RejectedExecutionException("New task rejected because completionTracker was already shut down");
            }
        }

        CompletableFuture<?> future = CompletableFuture.runAsync(runnable, executor);
        synchronized (this) {
            pending.put(counter, future);
            future.whenComplete((x, y) -> {
                synchronized (this) {
                    pending.remove(counter);
                    checkTerminationCondition();
                }
            });
            ++counter;
        }
    }

    public boolean isTerminated() {
        return isShutDown && pending.isEmpty();
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
