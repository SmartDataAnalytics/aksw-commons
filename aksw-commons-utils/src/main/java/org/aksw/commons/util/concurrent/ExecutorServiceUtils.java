package org.aksw.commons.util.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceUtils {

    /**
     * Create a default thread pool executor with twice as many threads as there are
     * processors reported by the JVM (assumes hyperthreading), and allowing a queue size of
     * 10 per thread.
     *
     * @return
     */
    public static ExecutorService newBlockingThreadPoolExecutor() {
        int numThreads = Runtime.getRuntime().availableProcessors() * 2;
        int queueSize = numThreads * 10;
        return ExecutorServiceUtils.newBlockingThreadPoolExecutor(numThreads, queueSize);
    }


    /**
     * A util method from stack overflow (link?) to avoid unbounding queuing of tasks
     * Submitting a task to an executor when its queue is full will block.
     */
    public static ExecutorService newBlockingThreadPoolExecutor(int threads, int queueSize) {
        ExecutorService result = new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(queueSize), new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                // This will block if the queue is full
                try {
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                    // System.err.println(e.getMessage());
                }

            }
        });

        return result;
    }
}
