package org.aksw.commons.util.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCompletionTracker {
    private static final Logger logger = LoggerFactory.getLogger(TestCompletionTracker.class);

    /** The completion tracker is expected to terminate on task failure */
    @Test(expected = RuntimeException.class)
    public void test1() throws InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool();
        try  {
            CompletionTracker c = CompletionTracker.from(es);


            new Thread(() -> {
                c.execute(() -> logger.debug("The next task fails on purpose"));
                c.execute(() -> { logger.debug("On-purpose task failure imminent"); throw new RuntimeException("Expected failure"); });
                for (;;) {
                    c.execute(() -> logger.debug("Repeatedly submitting this task should eventually fail to submit due to prior error"));
                }
            }).start();

            c.awaitTermination();

        } finally {
            es.shutdown();
        }
    }
}
